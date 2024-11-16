package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.utils.TranslucentRenderTypeBuffer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {
    private static final int UPDATE_INTERVAL = 500;
    private static long lastUpdate = 0;
    private static int lastSize = 0;
    private static List<BlockEntry> cachedBlocks = new ArrayList<>();
    private static final ModelData EMPTY_MODEL = ModelData.EMPTY;
    private static final BlockPos.MutableBlockPos MUTABLE_POS = new BlockPos.MutableBlockPos();
    private static final double MAX_RENDER_DIST_SQ = 64 * 64;

    private static class BlockEntry {
        final BlockPos pos;
        final Block block;
        double distSq;

        BlockEntry(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        final ClientLevel level = mc.level;
        final BlockRenderDispatcher renderer = mc.getBlockRenderer();
        final MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        final Vec3 camera = event.getCamera().getPosition();
        final PoseStack poseStack = event.getPoseStack();
        final int alpha = mc.player.getMainHandItem().is(CFItemTags.WRENCHES) ? 120 : 255;

        long time = System.currentTimeMillis();
        int currentSize = ClientCamoManager.CAMOUFLAGED_BLOCKS.size();
        if (time - lastUpdate > UPDATE_INTERVAL || currentSize != lastSize) {
            updateCache(camera);
            lastUpdate = time;
            lastSize = currentSize;
        }

        for (BlockEntry entry : cachedBlocks) {
            if (entry == null || entry.distSq > MAX_RENDER_DIST_SQ) continue;

            BlockState framedBlock = level.getBlockState(entry.pos);
            if (framedBlock.isAir()) continue;

            BlockState state = entry.block != null ? entry.block.defaultBlockState() : framedBlock;
            if (state.getRenderShape() != RenderShape.MODEL) continue;

            poseStack.pushPose();
            try {
                poseStack.translate(
                        entry.pos.getX() - camera.x(),
                        entry.pos.getY() - camera.y(),
                        entry.pos.getZ() - camera.z()
                );

                BakedModel model = renderer.getBlockModel(state);
                for (RenderType type : model.getRenderTypes(state, level.random, EMPTY_MODEL)) {
                    renderBlock(renderer, state, entry.pos, level, poseStack,
                            new TranslucentRenderTypeBuffer(bufferSource, alpha).getBuffer(type),
                            model, type);
                }
                bufferSource.endBatch();
            } finally {
                poseStack.popPose();
            }
        }
    }

    private static void updateCache(Vec3 camera) {
        cachedBlocks.clear();
        for (Map.Entry<BlockPos, Block> entry : ClientCamoManager.CAMOUFLAGED_BLOCKS.entrySet()) {
            if (entry.getKey() == null) continue;
            BlockEntry blockEntry = new BlockEntry(entry.getKey(), entry.getValue());
            blockEntry.distSq = entry.getKey().distToCenterSqr(camera);
            if (blockEntry.distSq <= MAX_RENDER_DIST_SQ) {
                cachedBlocks.add(blockEntry);
            }
        }
        cachedBlocks.sort((a, b) -> Double.compare(a.distSq, b.distSq));
    }

    private static void renderBlock(BlockRenderDispatcher dispatcher, BlockState state,
                                    BlockPos pos, ClientLevel level, PoseStack stack,
                                    VertexConsumer consumer, BakedModel model, RenderType type) {
        try {
            dispatcher.getModelRenderer().tesselateBlock(
                    level, model, state, pos, stack, consumer, true,
                    level.random, state.getSeed(pos), OverlayTexture.NO_OVERLAY,
                    EMPTY_MODEL, type
            );
        } catch (Throwable throwable) {
            CrashReport report = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory category = report.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(category, level, pos, state);
            throw new ReportedException(report);
        }
    }
}