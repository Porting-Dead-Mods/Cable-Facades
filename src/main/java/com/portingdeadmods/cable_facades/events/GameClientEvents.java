package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.TranslucentRenderTypeBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
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
            LocalPlayer player = mc.player;
            Vec3 cameraPos = event.getCamera().getPosition();
            BlockPos cameraBlockPos = new BlockPos((int) cameraPos.x(), (int) cameraPos.y(), (int) cameraPos.z());
            List<Map.Entry<BlockPos, Block>> sortedBlocks = CAMOUFLAGED_BLOCKS.entrySet().stream()
                    .sorted(Comparator.comparingDouble(entry -> entry.getKey().distSqr(cameraBlockPos)))
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
                    if(mc.player.getMainHandItem().getTags().anyMatch(itemTag -> itemTag.equals(CFItemTags.WRENCHES))){
                        for (RenderType type : mc.getBlockRenderer().getBlockModel(state).getRenderTypes(state,mc.level.random,ModelData.EMPTY)){
                            mc.getBlockRenderer().renderBatched(state, blockPos, mc.level, poseStack, new TranslucentRenderTypeBuffer(mc.renderBuffers().bufferSource(), 120).getBuffer(type), true, mc.level.random, ModelData.EMPTY, type);
                        }
                    }else{
                        for (RenderType type : mc.getBlockRenderer().getBlockModel(state).getRenderTypes(state, mc.level.random, ModelData.EMPTY)) {
                            mc.getBlockRenderer().renderBatched(state, blockPos, mc.level, poseStack, bufferSource.getBuffer(type), true, mc.level.random, ModelData.EMPTY, type);
                        }
                    }
                    bufferSource.endBatch();
                }
                poseStack.popPose();
            }
        }
    }


    private static BlockState getState(Block block, BlockState framedBlock) {
        return block != null ? block.defaultBlockState() : framedBlock;
    }
}
