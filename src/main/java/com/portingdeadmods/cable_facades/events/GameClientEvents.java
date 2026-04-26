package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.render.FacadePreviewRenderer;
import com.portingdeadmods.cable_facades.client.render.FacadeRenderConstants;
import com.portingdeadmods.cable_facades.client.render.FacadeRenderer;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {

    public static final ThreadLocal<Boolean> RENDERING_FACADE = ThreadLocal.withInitial(() -> false);

    public static boolean facadeTransparency = false;
    public static boolean setFacadeTransparency = false;
    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::create);
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Cable Facades Reset Timer");
        t.setDaemon(true);
        return t;
    });
    private static ScheduledFuture<?> resetFuture;

    @SubscribeEvent
    public static void render(RenderLevelStageEvent.AfterTranslucentBlocks event) {
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
                }, FacadeRenderConstants.TRANSPARENCY_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } else if (resetFuture != null) {
                resetFuture.cancel(false);
                resetFuture = null;
            }
        }
    }

    private record FacadeDataAndPos(FacadeData data, BlockPos pos) {
    }

    @SubscribeEvent
    public static void geometryEvent(AddSectionGeometryEvent e) {
        SectionPos section = SectionPos.of(e.getSectionOrigin());

        if (ClientFacadeManager.isEmpty()) return;

        List<FacadeDataAndPos> sectionFacades = new ArrayList<>();

        Iterable<Map.Entry<BlockPos, FacadeData>> entryIterator = ClientFacadeManager.entryIterator();

        for (Map.Entry<BlockPos, FacadeData> entry : entryIterator) {
            BlockPos pos = entry.getKey();
            FacadeData data = entry.getValue();
            if (SectionPos.of(pos).equals(section)) {
                sectionFacades.add(new FacadeDataAndPos(data, pos));
            }
        }

        if (sectionFacades.isEmpty()) return;

        e.addRenderer(ctx -> {
            RENDERING_FACADE.set(true);
            BlockAndTintGetter level = ctx.getRegion();
            RandomSource random = RANDOM.get();

            for (FacadeDataAndPos sectionFacade : sectionFacades) {
                BlockPos pos = sectionFacade.pos();
                FacadeData facadeData = sectionFacade.data();

                Minecraft mc = Minecraft.getInstance();
                boolean cutoutLeaves = mc.options.cutoutLeaves().get();
                BlockQuadOutput quadOutput = (x, y, z, quad, instance) -> {
                    VertexConsumer builder = ctx.getOrCreateChunkBuffer(quad.materialInfo().layer());
                    builder.putBlockBakedQuad(x, y, z, quad, instance);
                };
                BlockQuadOutput opaqueQuadOutput = (x, y, z, quad, instance) -> {
                    VertexConsumer builder = ctx.getOrCreateChunkBuffer(ChunkSectionLayer.SOLID);
                    builder.putBlockBakedQuad(x, y, z, quad, instance);
                };

                if (facadeData.isFullBlock()) {
                    BlockState fullBlockState = facadeData.getFullBlock();
                    BlockQuadOutput finalOutput = ModelBlockRenderer.forceOpaque(cutoutLeaves, fullBlockState) ? opaqueQuadOutput : quadOutput;
                    FacadeRenderer.renderFullBlockFacade(finalOutput, ctx.getPoseStack(), ctx.getBlockRenderer(), level, random, pos, fullBlockState);
                } else if (facadeData.isDirectional()) {
                    for (Map.Entry<Direction, BlockState> mapEntry : facadeData.directional().entrySet()) {
                        Direction dir = mapEntry.getKey();
                        BlockState state = mapEntry.getValue();
                        BlockQuadOutput finalOutput = ModelBlockRenderer.forceOpaque(cutoutLeaves, state) ? opaqueQuadOutput : quadOutput;
                        FacadeRenderer.renderDirectionalFacade(finalOutput, ctx.getPoseStack(), ctx.getBlockRenderer(), level, random, pos, dir, state);
                    }
                }
            }

            RENDERING_FACADE.set(false);
        });
    }

//    private static void renderModel(AddSectionGeometryEvent.SectionRenderingContext ctx,
//                                    BlockAndLightGetter level, RandomSource random,
//                                    BlockPos pos, BlockState facadeState,
//                                    BakedModel facadeModel, ModelData modelData, PoseStack poseStack) {
//        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//
//        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
//            VertexConsumer buffer = ctx.getOrCreateChunkBuffer(facadeTransparency ? RenderType.translucent() : renderType);
//            if (facadeTransparency) {
//                buffer = CFMain.isIrisLoaded() ? new AlphaWrapperIris(buffer) : new AlphaWrapper(buffer);
//            }
//            if (CFMain.isIrisLoaded()) {
//                IrisUtil.beginBlock(buffer, facadeState, pos);
//            }
//            blockRenderer.renderBatched(facadeState, pos, level, poseStack, buffer, true, random, modelData, renderType);
//            if (CFMain.isIrisLoaded()) {
//                IrisUtil.endBlock(buffer);
//            }
//        }
//    }

    @SubscribeEvent
    public static void renderOutline(ExtractBlockOutlineRenderStateEvent event) {
        event.addCustomRenderer((renderState, buffer, poseStack, translucentPass, levelRenderState) -> {
            Level world = Minecraft.getInstance().level;
            HitResult rtr = Minecraft.getInstance().hitResult;
            if (rtr.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) rtr;
                Camera camera = event.getCamera();
                return FacadePreviewRenderer.renderPlacementPreview(poseStack, buffer, world, Minecraft.getInstance().player, camera, blockHitResult);
            }

            return false;
        });
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ClientFacadeManager.clear();
    }
}
