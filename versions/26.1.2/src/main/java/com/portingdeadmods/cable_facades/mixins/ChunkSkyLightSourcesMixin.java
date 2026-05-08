package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkSkyLightSources.class)
public class ChunkSkyLightSourcesMixin {

    @WrapOperation(
            method = {"update", "findLowestSourceBelow"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState cable_facades$facadeAwareGetBlockState(BlockGetter level, BlockPos pos, Operation<BlockState> original) {
        BlockState originalState = original.call(level, pos);
        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data != null && data.isFullBlock()) {
            return data.getFullBlock();
        }
        return originalState;
    }
}
