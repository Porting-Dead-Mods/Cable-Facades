package com.portingdeadmods.cable_facades.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
public class BlockHidingMixin {
    @Inject(
            method = "renderBatched*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderBatched(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack poseStack,
                                 VertexConsumer consumer, boolean checkSides, RandomSource random, ModelData modelData,
                                 RenderType renderType, CallbackInfo ci) {

        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data == null) return;

        if (data.isFullBlock() && CFConfig.shouldHideWhenFacaded(state.getBlock())) {
            ci.cancel();
        }
    }
}
