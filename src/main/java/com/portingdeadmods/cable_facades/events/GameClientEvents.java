package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.render.CoverQuadRenderer;
import com.portingdeadmods.cable_facades.client.render.FacadeQuadLighter;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.mixins.LevelRendererAccess;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFRenderTypes;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeItemNbt;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {

    public static final ThreadLocal<Boolean> RENDERING_FACADE = ThreadLocal.withInitial(() -> false);

    private static final long TRANSPARENCY_TIMEOUT_MS = 120_000L;
    private static final float ZFIGHTING_SCALE = 0.99995F;
    private static final float SCALE_UP_FACTOR = 1.0005F;
    private static final int ALPHA_MASK = 0x88FFFFFF;
    private static final int PREVIEW_ALPHA = 0x99000000;
    private static final long FACADE_RENDER_SEED = 42L;

    public static boolean facadeTransparency = false;
    public static boolean setFacadeTransparency = false;

    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Cable Facades Reset Timer");
        t.setDaemon(true);
        return t;
    });
    private static ScheduledFuture<?> resetFuture;

    private GameClientEvents() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (setFacadeTransparency != facadeTransparency) {
            setFacadeTransparency = facadeTransparency;
            Set<SectionPos> sections = new ObjectOpenHashSet<>();
            ClientFacadeManager.forEach((pos, data) -> sections.add(SectionPos.of(pos)));
            for (SectionPos section : sections) {
                Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            }
            if (facadeTransparency) {
                if (resetFuture != null) resetFuture.cancel(false);
                resetFuture = TIMER.schedule(() -> {
                    CFMain.LOGGER.info("Facades made opaque due to timeout");
                    facadeTransparency = false;
                }, TRANSPARENCY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } else if (resetFuture != null) {
                resetFuture.cancel(false);
                resetFuture = null;
            }
        }
    }

    @SubscribeEvent
    public static void renderOutline(RenderHighlightEvent.Block event) {
        if (event.getCamera().getEntity() instanceof LivingEntity living) {
            Level world = living.level();
            BlockHitResult rtr = event.getTarget();
            renderPlacementPreview(event, world, rtr, living);
            BlockPos pos = rtr.getBlockPos();
            Vec3 renderView = event.getCamera().getPosition();
            BlockState targetBlock = world.getBlockState(rtr.getBlockPos());

            if (FacadeUtils.hasFacade(world, pos)) {
                ((LevelRendererAccess) event.getLevelRenderer()).callRenderHitOutline(
                        event.getPoseStack(), event.getMultiBufferSource().getBuffer(CFRenderTypes.LINES_NONTRANSLUCENT),
                        living, renderView.x, renderView.y, renderView.z,
                        pos, targetBlock
                );
                event.setCanceled(true);
            }
        }
    }

    private static void renderPlacementPreview(RenderHighlightEvent.Block event, Level world, BlockHitResult hit, LivingEntity living) {
        if (!(living instanceof Player player)) return;
        PlacementPreview preview = getPlacementPreview(player, hit);
        if (preview == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(
                preview.pos().getX() - camera.x,
                preview.pos().getY() - camera.y,
                preview.pos().getZ() - camera.z
        );

        Block facadedBlock = world.getBlockState(preview.pos()).getBlock();
        if (CFConfig.canPatchZFighting(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(ZFIGHTING_SCALE, ZFIGHTING_SCALE, ZFIGHTING_SCALE);
            poseStack.translate(-0.5, -0.5, -0.5);
        }
        if (CFConfig.isScaleUpBlock(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(SCALE_UP_FACTOR, SCALE_UP_FACTOR, SCALE_UP_FACTOR);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        if (preview.directional()) {
            renderDirectionalPreview(event, world, preview.pos(), preview.face(), preview.facadeState(), poseStack);
        } else {
            renderFullBlockPreview(event, world, preview.pos(), preview.facadeState(), poseStack);
        }
        poseStack.popPose();
    }

    private static void renderFullBlockPreview(RenderHighlightEvent.Block event, Level world, BlockPos pos,
                                               BlockState facadeState, PoseStack poseStack) {
        RandomSource random = RandomSource.create(FACADE_RENDER_SEED);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
        ModelData modelData = facadeModel.getModelData(world, pos, facadeState, ModelData.EMPTY);

        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
            VertexConsumer buffer = new PreviewAlphaWrapper(event.getMultiBufferSource().getBuffer(RenderType.translucent()));
            blockRenderer.renderBatched(facadeState, pos, world, poseStack, buffer, true, random, modelData, renderType);
        }
    }

    private static void renderDirectionalPreview(RenderHighlightEvent.Block event, Level world, BlockPos pos,
                                                 Direction face, BlockState facadeState, PoseStack poseStack) {
        RandomSource random = RandomSource.create(FACADE_RENDER_SEED);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
        ModelData modelData = facadeModel.getModelData(world, pos, facadeState, ModelData.EMPTY);
        boolean useAo = Minecraft.useAmbientOcclusion()
                && facadeState.getLightEmission() == 0
                && facadeModel.useAmbientOcclusion();
        List<BakedQuad> slicedQuads = CoverQuadRenderer.sliceQuads(facadeState, pos, facadeModel, face, modelData);

        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
            VertexConsumer buffer = new PreviewAlphaWrapper(event.getMultiBufferSource().getBuffer(RenderType.translucent()));
            for (BakedQuad quad : slicedQuads) {
                float r = 1f, g = 1f, b = 1f;
                if (quad.getTintIndex() != -1) {
                    int color = Minecraft.getInstance().getBlockColors().getColor(facadeState, world, pos, quad.getTintIndex());
                    r = (color >> 16 & 0xFF) / 255f;
                    g = (color >> 8 & 0xFF) / 255f;
                    b = (color & 0xFF) / 255f;
                }
                FacadeQuadLighter.renderQuad(world, facadeState, pos, poseStack.last(), buffer, quad, r, g, b,
                        OverlayTexture.NO_OVERLAY, useAo);
            }
        }
    }

    private static PlacementPreview getPlacementPreview(Player player, BlockHitResult hit) {
        PlacementPreview preview = getPlacementPreview(player, InteractionHand.MAIN_HAND, hit);
        return preview != null ? preview : getPlacementPreview(player, InteractionHand.OFF_HAND, hit);
    }

    private static PlacementPreview getPlacementPreview(Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        boolean directional = stack.getItem() instanceof DirectionalFacadeItem;
        if (!(stack.getItem() instanceof FacadeItem) && !directional) {
            return null;
        }
        Level level = player.level();
        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        BlockState existingFullFacade = FacadeUtils.getFacade(level, pos);
        BlockState existingDirectionalFace = FacadeUtils.getDirectionalFacade(level, pos, face);

        if (!directional && FacadeUtils.hasFacade(level, pos)) return null;
        if (directional && (existingFullFacade != null || existingDirectionalFace != null)) return null;

        Block facadeBlock = resolveFacadeBlock(player, hand, stack);
        if (facadeBlock == null || !(facadeBlock.asItem() instanceof BlockItem)) return null;

        Block targetBlock = level.getBlockState(pos).getBlock();
        boolean noFacadeTag = level.getBlockState(pos).getTags()
                .noneMatch(tag -> tag.equals(CFItemTags.SUPPORTS_FACADE));

        if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) return null;
        if (targetBlock == facadeBlock || CFConfig.isBlockDisallowed(facadeBlock)) return null;

        BlockState previewState = facadeBlock.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, hit)));
        if (previewState == null) {
            previewState = facadeBlock.defaultBlockState();
        }
        return new PlacementPreview(pos, face, previewState, directional);
    }

    private static Block resolveFacadeBlock(Player player, InteractionHand hand, ItemStack stack) {
        Block block = FacadeItemNbt.getFacadeBlock(stack);
        if (block != null) return block;
        if (hand != InteractionHand.MAIN_HAND) return null;

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return null;
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ClientFacadeManager.clear();
    }

    public static class AlphaWrapper extends VertexConsumerWrapper {
        public AlphaWrapper(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return super.color(red, green, blue, Math.min(alpha, 0x88));
        }
    }

    public static class PreviewAlphaWrapper extends VertexConsumerWrapper {
        public PreviewAlphaWrapper(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return super.color(red, green, blue, (PREVIEW_ALPHA >> 24) & 0xFF);
        }
    }

    public record PlacementPreview(BlockPos pos, Direction face, BlockState facadeState, boolean directional) {}
}
