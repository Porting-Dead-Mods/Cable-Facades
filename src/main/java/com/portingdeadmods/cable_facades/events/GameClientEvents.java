package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.mixins.LevelRendererAccess;
import com.portingdeadmods.cable_facades.registries.CFRenderTypes;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {

    public static final ThreadLocal<Boolean> RENDERING_FACADE = ThreadLocal.withInitial(() -> false);
    public static boolean facadeTransparency = false;
    public static boolean setFacadeTransparency = false;
    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::create);
    private static Timer resetTimer;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if (setFacadeTransparency != facadeTransparency) {
            setFacadeTransparency = facadeTransparency;
            Set<SectionPos> sections = new ObjectOpenHashSet<>();

            ClientFacadeManager.FACADED_BLOCKS.keySet().forEach(k -> {
                sections.add(SectionPos.of(k));
            });

            for (SectionPos section : sections) {
                Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            }

            if (facadeTransparency) {
                if (resetTimer != null) resetTimer.cancel();

                resetTimer = new Timer("Cable Facades Reset Timer");

                resetTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        CFMain.LOGGER.info("Facades made opaque due to timeout");
                        facadeTransparency = false;
                    }
                }, 120000);
            } else {
                resetTimer.cancel();
            }
        }
    }

    @SubscribeEvent
    public static void geometryEvent(AddSectionGeometryEvent e) {
        SectionPos section = SectionPos.of(e.getSectionOrigin());

        if (ClientFacadeManager.FACADED_BLOCKS.isEmpty()) return;

        Map<BlockPos, @Nullable BlockState> actualBlocks = new Object2ObjectOpenHashMap<>();

        ClientFacadeManager.FACADED_BLOCKS.entrySet().stream().filter(p -> SectionPos.of(p.getKey()).equals(section)).forEachOrdered(bp -> {
            if (bp.getValue() != null) actualBlocks.put(bp.getKey(), bp.getValue());
        });

        if (actualBlocks.isEmpty()) return;

        e.addRenderer(new AddSectionGeometryEvent.AdditionalSectionRenderer() {
            @Override
            public void render(AddSectionGeometryEvent.SectionRenderingContext sectionRenderingContext) {
                RENDERING_FACADE.set(true);
                BlockAndTintGetter level = sectionRenderingContext.getRegion();
                RandomSource random = RANDOM.get();

                for (Map.Entry<BlockPos, @Nullable BlockState> blockPosBlockStateEntry : actualBlocks.entrySet()) {
                    random.setSeed(42L);
                    //System.out.println("Rendering facade at " + blockPosBlockStateEntry.getKey());
                    BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                    BlockState facadeState = blockPosBlockStateEntry.getValue();
                    BlockPos pos = blockPosBlockStateEntry.getKey();
                    PoseStack poseStack = sectionRenderingContext.getPoseStack();

                    BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
                    ModelData modelData = facadeModel.getModelData(level, pos, facadeState, ModelData.EMPTY);

                    // Offset the pose stack for the facade position
                    poseStack.pushPose();
                    poseStack.translate(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));


                    for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
                        VertexConsumer buffer = sectionRenderingContext.getOrCreateChunkBuffer(facadeTransparency ? RenderType.translucent() : renderType);
                        blockRenderer.renderBatched(facadeState, pos, level, poseStack, buffer, true, random, modelData, renderType);
                    }

                    poseStack.popPose();
                }

                RENDERING_FACADE.set(false);
            }
        });
    }

    // From Immersive Engineering. Thank you blu, for figuring out this fix <3
    @SubscribeEvent
    public static void renderOutline(RenderHighlightEvent.Block event) {
        if (event.getCamera().getEntity() instanceof LivingEntity living) {
            Level world = living.level();
            BlockHitResult rtr = event.getTarget();
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

    public static void loadChunk() {

    }
}