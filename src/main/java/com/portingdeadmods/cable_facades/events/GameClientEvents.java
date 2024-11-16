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
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }

        ClientLevel level = mc.level;
        Vec3 cameraPos = event.getCamera().getPosition();
        if (cameraPos == null) {
            return;
        }

        Frustum frustum = event.getFrustum();

        if (ClientCamoManager.CAMOUFLAGED_BLOCKS == null || ClientCamoManager.CAMOUFLAGED_BLOCKS.isEmpty()) {
            return;
        }

        List<Map.Entry<BlockPos, Block>> sortedBlocks = ClientCamoManager.CAMOUFLAGED_BLOCKS.entrySet().stream()
                .filter(entry -> {
                    BlockPos pos = entry.getKey();
                    if (pos == null) return false;
                    AABB boundingBox = new AABB(pos);
                    return frustum.isVisible(boundingBox);
                })
                .sorted(Comparator.comparingDouble(entry ->
                        entry.getKey().distToCenterSqr(cameraPos)))
                .toList();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        if (bufferSource == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        if (poseStack == null) {
            return;
        }

        for (Map.Entry<BlockPos, Block> entry : sortedBlocks) {
            BlockPos blockPos = entry.getKey();
            Block block = entry.getValue();
            if (blockPos == null || block == null) {
                continue;
            }

            BlockState framedBlock = level.getBlockState(blockPos);
            if (framedBlock == null) {
                continue;
            }

            poseStack.pushPose();
            try {
                poseStack.translate(
                        blockPos.getX() - cameraPos.x(),
                        blockPos.getY() - cameraPos.y(),
                        blockPos.getZ() - cameraPos.z()
                );

                BlockState state = getState(block, framedBlock);
                if (state == null) {
                    continue;
                }

                BakedModel blockModel = mc.getBlockRenderer().getBlockModel(state);
                if (blockModel == null) {
                    continue;
                }

                ModelData modelData = blockModel.getModelData(level, blockPos, state, ModelData.EMPTY);
                boolean isHoldingWrench = mc.player.getMainHandItem().is(CFItemTags.WRENCHES);

                for (RenderType type : blockModel.getRenderTypes(state, level.random, modelData)) {
                    if (type == null) continue;

                    TranslucentRenderTypeBuffer translucentBuffer = new TranslucentRenderTypeBuffer(
                            bufferSource,
                            isHoldingWrench ? 120 : 255
                    );

                    VertexConsumer vertexConsumer = translucentBuffer.getBuffer(type);
                    if (vertexConsumer == null) continue;

                    renderBatched(
                            mc.getBlockRenderer(),
                            state,
                            blockPos,
                            level,
                            poseStack,
                            vertexConsumer,
                            true,
                            level.random,
                            modelData,
                            type
                    );
                }

                bufferSource.endBatch();
            } finally {
                poseStack.popPose();
            }
        }
    }

    private static void renderBatched(
            BlockRenderDispatcher blockRenderDispatcher,
            BlockState state,
            BlockPos pos,
            BlockAndTintGetter level,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            boolean checkSides,
            RandomSource random,
            ModelData modelData,
            RenderType renderType) {

        if (blockRenderDispatcher == null || state == null || pos == null ||
                level == null || poseStack == null || vertexConsumer == null ||
                random == null || modelData == null || renderType == null) {
            return;
        }

        try {
            RenderShape rendershape = state.getRenderShape();
            if (rendershape == RenderShape.MODEL) {
                BakedModel blockModel = blockRenderDispatcher.getBlockModel(state);
                if (blockModel != null) {
                    blockRenderDispatcher.getModelRenderer().tesselateBlock(
                            level,
                            blockModel,
                            state,
                            pos,
                            poseStack,
                            vertexConsumer,
                            checkSides,
                            random,
                            state.getSeed(pos),
                            OverlayTexture.NO_OVERLAY,
                            modelData,
                            renderType
                    );
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, level, pos, state);
            throw new ReportedException(crashreport);
        }
    }

    private static BlockState getState(Block block, BlockState framedBlock) {
        if (block == null && framedBlock == null) {
            return null;
        }
        return block != null ? block.defaultBlockState() : framedBlock;
    }
}