package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModelLighter.class)
public class BlockModelLighterMixin {

    @WrapOperation(
            method = "prepareQuadAmbientOcclusion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightDampening()I")
    )
    private int cable_facades$facadeAwareGetLightDampening(BlockState cornerState, Operation<Integer> original,
                                                           @Local(argsOnly = true) BlockAndTintGetter level,
                                                           @Local BlockPos.MutableBlockPos pos) {
        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data != null && data.isFullBlock()) {
            return data.getFullBlock().getLightDampening();
        }
        return original.call(cornerState);
    }

    @WrapOperation(
            method = "prepareQuadAmbientOcclusion",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isSolidRender()Z")
    )
    private boolean cable_facades$facadeAwareIsSolidRender(BlockState nextState, Operation<Boolean> original,
                                                           @Local(argsOnly = true) BlockAndTintGetter level,
                                                           @Local BlockPos.MutableBlockPos pos) {
        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data != null) {
            if (data.isFullBlock()) {
                return data.getFullBlock().isSolidRender();
            }
            return false;
        }
        return original.call(nextState);
    }
}
