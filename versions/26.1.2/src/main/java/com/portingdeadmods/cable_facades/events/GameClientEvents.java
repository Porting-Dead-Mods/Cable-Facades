package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.render.FacadeChunkRenderer;
import com.portingdeadmods.cable_facades.client.render.FacadePreviewRenderer;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.mixins.BlockBehaviourAccess;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFRenderTypes;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {

    private static final long TRANSPARENCY_TIMEOUT_MS = 120_000L;
    private static final int ALPHA_MASK = 0x88FFFFFF;
    private static final int PREVIEW_ALPHA = 0x99000000;

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
    public static void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (setFacadeTransparency != facadeTransparency) {
            setFacadeTransparency = facadeTransparency;
            Set<SectionPos> sections = new ObjectOpenHashSet<>();

            ClientFacadeManager.forEach((pos, data) -> {
                SectionPos center = SectionPos.of(pos);
                sections.add(center);
                int localX = pos.getX() & 15;
                int localY = pos.getY() & 15;
                int localZ = pos.getZ() & 15;
                if (localX == 0)  sections.add(SectionPos.of(center.x() - 1, center.y(), center.z()));
                if (localX == 15) sections.add(SectionPos.of(center.x() + 1, center.y(), center.z()));
                if (localY == 0)  sections.add(SectionPos.of(center.x(), center.y() - 1, center.z()));
                if (localY == 15) sections.add(SectionPos.of(center.x(), center.y() + 1, center.z()));
                if (localZ == 0)  sections.add(SectionPos.of(center.x(), center.y(), center.z() - 1));
                if (localZ == 15) sections.add(SectionPos.of(center.x(), center.y(), center.z() + 1));
            });

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
    public static void geometryEvent(AddSectionGeometryEvent e) {
        SectionPos section = SectionPos.of(e.getSectionOrigin());

        if (ClientFacadeManager.isEmpty()) return;

        Map<BlockPos, FacadeData> sectionFacades = new Object2ObjectOpenHashMap<>();

        ClientFacadeManager.entryStream()
                .filter(p -> SectionPos.of(p.getKey()).equals(section))
                .forEach(bp -> {
                    if (bp.getValue() != null) sectionFacades.put(bp.getKey(), bp.getValue());
                });

        if (sectionFacades.isEmpty()) return;

        e.addRenderer(ctx -> {
            BlockAndTintGetter level = ctx.getRegion();
            for (Map.Entry<BlockPos, FacadeData> entry : sectionFacades.entrySet()) {
                BlockPos pos = entry.getKey();
                FacadeData facadeData = entry.getValue();

                if (facadeData.isFullBlock()) {
                    FacadeChunkRenderer.renderFullBlock(ctx, level, pos, facadeData.getFullBlock(), facadeTransparency);
                } else if (facadeData.isDirectional()) {
                    facadeData.directional().forEach((dir, state) ->
                            FacadeChunkRenderer.renderDirectional(ctx, level, pos, dir, state, facadeTransparency));
                }
            }
        });
    }

    @SubscribeEvent
    public static void renderOutline(ExtractBlockOutlineRenderStateEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        PlacementPreview preview = getPlacementPreview(player, event.getHitResult());
        BlockPos hitPos = event.getBlockPos();
        boolean facaded = FacadeUtils.hasFacade(event.getLevel(), hitPos);

        if (preview != null) {
            event.addCustomRenderer((renderState, buffer, poseStack, translucentPass, levelRenderState) -> {
                renderPlacementPreview(buffer, poseStack, event.getCamera().position(), preview);
                return false;
            });
        }

        if (facaded) {
            BlockState cableState = event.getLevel().getBlockState(hitPos);
            VoxelShape cableShape = facadeAwareCableShape(event.getLevel(), hitPos, cableState);
            event.addCustomRenderer((renderState, buffer, poseStack, translucentPass, levelRenderState) ->
                    renderCableOutline(buffer, poseStack, event.getCamera().position(), hitPos, cableShape));
        }
    }

    private static VoxelShape facadeAwareCableShape(ClientLevel level, BlockPos pos, BlockState cableState) {
        // Bypass our BlockStateMixin/BlockStateBaseMixin (which would return the multipart facade shape)
        // by invoking the protected BlockBehaviour.getShape directly via accessor mixin.
        return ((BlockBehaviourAccess) cableState.getBlock()).cableFacades$invokeGetShape(cableState, level, pos, CollisionContext.empty());
    }

    private static boolean renderCableOutline(MultiBufferSource.BufferSource buffer, PoseStack poseStack,
                                              Vec3 camera, BlockPos pos, VoxelShape shape) {
        VertexConsumer lines = buffer.getBuffer(CFRenderTypes.LINES_NONTRANSLUCENT);
        ShapeRenderer.renderShape(
                poseStack, lines, shape,
                pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z,
                0xFF000000, 1.0F
        );
        return true;
    }

    private static boolean renderPlacementPreview(MultiBufferSource.BufferSource buffer, PoseStack poseStack,
                                                  Vec3 camera, PlacementPreview preview) {
        BlockPos pos = preview.pos();
        BlockState facadeState = preview.facadeState();
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return false;

        Block facadedBlock = level.getBlockState(pos).getBlock();
        float scale = computePreviewScale(facadedBlock);

        VertexConsumer buffer0 = new PreviewAlphaWrapper(buffer.getBuffer(Sheets.translucentBlockSheet()));
        BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(facadeState);

        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camera.x,
                pos.getY() - camera.y,
                pos.getZ() - camera.z
        );

        if (preview.directional()) {
            FacadePreviewRenderer.renderDirectional(buffer0, poseStack, level, pos, preview.face(), facadeState, model, scale);
        } else {
            FacadePreviewRenderer.renderFullBlock(buffer0, poseStack, level, pos, facadeState, model, scale);
        }

        poseStack.popPose();
        return true;
    }

    private static float computePreviewScale(Block facadedBlock) {
        float scale = 1.0F;
        if (CFConfig.canPatchZFighting(facadedBlock)) {
            scale *= 0.99995F;
        }
        if (CFConfig.isScaleUpBlock(facadedBlock)) {
            scale *= 1.0005F;
        }
        return scale;
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

        if (!directional && FacadeUtils.hasFacade(level, pos)) {
            return null;
        }
        if (directional && (existingFullFacade != null || existingDirectionalFace != null)) {
            return null;
        }

        Block facadeBlock = resolveFacadeBlock(player, hand, stack);
        if (facadeBlock == null || !(facadeBlock.asItem() instanceof BlockItem)) {
            return null;
        }

        BlockState targetState = level.getBlockState(pos);
        Block targetBlock = targetState.getBlock();
        boolean noFacadeTag = !targetState.is(CFItemTags.SUPPORTS_FACADE);

        if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) {
            return null;
        }
        if (targetBlock == facadeBlock || CFConfig.isBlockDisallowed(facadeBlock)) {
            return null;
        }

        BlockState previewState = facadeBlock.getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, hand, hit)));
        if (previewState == null) {
            previewState = facadeBlock.defaultBlockState();
        }

        return new PlacementPreview(pos, face, previewState, directional);
    }

    private static Block resolveFacadeBlock(Player player, InteractionHand hand, ItemStack stack) {
        Optional<Block> block = stack.get(CFDataComponents.FACADE_BLOCK);
        if (block != null && block.isPresent()) {
            return block.get();
        }
        if (hand != InteractionHand.MAIN_HAND) {
            return null;
        }

        ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock();
        }
        return null;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientFacadeManager.clear();
    }

    @SubscribeEvent
    public static void onDimensionChange(ClientPlayerNetworkEvent.Clone event) {
        ClientFacadeManager.clear();
    }

    static class AlphaWrapper extends VertexConsumerWrapper {
        public AlphaWrapper(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public VertexConsumer setColor(int color) {
            super.setColor(color & ALPHA_MASK);
            return this;
        }
    }

    static class PreviewAlphaWrapper extends VertexConsumerWrapper {
        public PreviewAlphaWrapper(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public VertexConsumer setColor(int color) {
            super.setColor((color & 0x00FFFFFF) | PREVIEW_ALPHA);
            return this;
        }
    }

    private record PlacementPreview(BlockPos pos, Direction face, BlockState facadeState, boolean directional) {
    }
}
