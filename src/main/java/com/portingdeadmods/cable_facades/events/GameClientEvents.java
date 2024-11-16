package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.utils.TranslucentRenderTypeBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class GameClientEvents {
    public static Map<BlockPos, @Nullable Block> CAMOUFLAGED_BLOCKS = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();
            Vec3 cameraPos = event.getCamera().getPosition();
            BlockPos cameraBlockPos = new BlockPos((int) cameraPos.x(), (int) cameraPos.y(), (int) cameraPos.z());
            List<Map.Entry<BlockPos, Block>> sortedBlocks = CAMOUFLAGED_BLOCKS.entrySet().stream()
                    .sorted(Comparator.comparingDouble(entry -> entry.getKey().distToCenterSqr(event.getCamera().getPosition())))
                    .toList();
            for (Map.Entry<BlockPos, Block> entry : sortedBlocks) {
                BlockPos blockPos = entry.getKey();
                Block block = entry.getValue();
                ClientLevel level = mc.level;
                BlockState framedBlock = level.getBlockState(blockPos);
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();
                {
                    poseStack.translate(blockPos.getX() - cameraPos.x(), blockPos.getY() - cameraPos.y(), blockPos.getZ() - cameraPos.z());
                    BlockState state = getState(block, framedBlock);
                    MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
                    BakedModel blockModel = mc.getBlockRenderer().getBlockModel(state);
                    if (mc.player.getMainHandItem().is(CFItemTags.WRENCHES)) {
                        for (RenderType type : blockModel.getRenderTypes(state, mc.level.random, blockModel.getModelData(mc.level, blockPos, state, ModelData.EMPTY))) {
                            renderBatched(mc.getBlockRenderer(), state, blockPos, mc.level, poseStack, new TranslucentRenderTypeBuffer(bufferSource, 120).getBuffer(type), true, mc.level.random, ModelData.EMPTY, type);
                        }
                    } else {
                        for (RenderType type : blockModel.getRenderTypes(state, mc.level.random, blockModel.getModelData(mc.level, blockPos, state, ModelData.EMPTY))) {
                            renderBatched(mc.getBlockRenderer(), state, blockPos, mc.level, poseStack, new TranslucentRenderTypeBuffer(bufferSource, 255).getBuffer(type), true, mc.level.random, ModelData.EMPTY, type);
                        }
                    }
                    bufferSource.endBatch();
                }
                poseStack.popPose();
            }
        }
    }

    private static void renderBatched(BlockRenderDispatcher blockRenderDispatcher, BlockState p_234356_, BlockPos p_234357_, BlockAndTintGetter p_234358_, PoseStack p_234359_, VertexConsumer p_234360_, boolean p_234361_, RandomSource p_234362_, ModelData modelData, RenderType renderType) {
        try {
            RenderShape rendershape = p_234356_.getRenderShape();
            if (rendershape == RenderShape.MODEL) {
                BakedModel blockModel = blockRenderDispatcher.getBlockModel(p_234356_);
                blockRenderDispatcher.getModelRenderer().tesselateBlock(p_234358_, blockModel, p_234356_, p_234357_, p_234359_, p_234360_, p_234361_, p_234362_, p_234356_.getSeed(p_234357_), OverlayTexture.NO_OVERLAY, blockModel.getModelData(p_234358_, p_234357_, p_234356_, ModelData.EMPTY), renderType);
            }

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, p_234358_, p_234357_, p_234356_);
            throw new ReportedException(crashreport);
        }
    }

    private static BlockState getState(Block block, BlockState framedBlock) {
        return block != null ? block.defaultBlockState() : framedBlock;
    }
}
