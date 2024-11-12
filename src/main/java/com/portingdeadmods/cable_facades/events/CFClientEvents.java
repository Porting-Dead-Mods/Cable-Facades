package com.portingdeadmods.cable_facades.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.CFMain;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
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

import java.util.Map;

@Mod.EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class CFClientEvents {
    public static Map<BlockPos, @Nullable Block> CAMOUFLAGED_BLOCKS = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            Minecraft mc = Minecraft.getInstance();

            for (Map.Entry<BlockPos, Block> entry : CAMOUFLAGED_BLOCKS.entrySet()) {
                BlockPos blockPos = entry.getKey();
                ClientLevel level = mc.level;
                BlockState framedBlock = level.getBlockState(blockPos);
                Vec3 vec3 = event.getCamera().getPosition();
                double d0 = vec3.x();
                double d1 = vec3.y();
                double d2 = vec3.z();

                PoseStack poseStack = event.getPoseStack();

                poseStack.pushPose();
                {
                    poseStack.translate((double) blockPos.getX() - d0, (double) blockPos.getY() - d1, (double) blockPos.getZ() - d2);
                    Block value = entry.getValue();
                    BlockState state = getState(value, framedBlock);
                    for (RenderType type : mc.getBlockRenderer().getBlockModel(state).getRenderTypes(state, mc.level.random, ModelData.EMPTY)) {
                        mc.getBlockRenderer().renderBatched(state, blockPos, mc.level, poseStack, mc.renderBuffers().bufferSource().getBuffer(type), true, mc.level.random, ModelData.EMPTY, type);
                    }
                }
                poseStack.popPose();
            }
        }
    }

    private static BlockState getState(Block block, BlockState framedBlock) {
        return block != null ? block.defaultBlockState() : framedBlock;
    }
}
