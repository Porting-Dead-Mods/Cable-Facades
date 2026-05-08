package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererFaceCullingMixin {

    @WrapOperation(
            method = "shouldRenderFace",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState cable_facades$facadeAwareNeighborState(BlockAndTintGetter level, BlockPos neighborPos, Operation<BlockState> original) {
        BlockState original_state = original.call(level, neighborPos);
        FacadeData data = FacadeUtils.getFacadeData(level, neighborPos);
        if (data != null && data.isFullBlock()) {
            return data.getFullBlock();
        }
        return original_state;
    }
}
