package com.portingdeadmods.cable_facades.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockRenderDispatcher.class)
public class BlockHidingMixin {
    @Inject(
            method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLjava/util/Random;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderBatched(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack,
                                 VertexConsumer consumer, boolean checkSides, Random random,
                                 CallbackInfoReturnable<Boolean> cir) {
        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data == null) return;
        if (data.isFullBlock() && CFConfig.shouldHideWhenFacaded(state.getBlock())) {
            cir.setReturnValue(true);
        }
    }
}
