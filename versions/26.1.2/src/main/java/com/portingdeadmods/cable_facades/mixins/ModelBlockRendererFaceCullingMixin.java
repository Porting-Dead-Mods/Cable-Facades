package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererFaceCullingMixin {

    @WrapOperation(
            method = "shouldRenderFace",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockAndTintGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState cable_facades$facadeAwareNeighborState(BlockAndTintGetter level, BlockPos neighborPos,
                                                             Operation<BlockState> original,
                                                             @Local(argsOnly = true) Direction direction) {
        BlockState originalState = original.call(level, neighborPos);
        if (GameClientEvents.facadeTransparency) {
            return originalState;
        }
        FacadeData data = FacadeUtils.getFacadeData(level, neighborPos);
        if (data == null) {
            return originalState;
        }
        BlockState facadeState = data.getFace(direction.getOpposite());
        return facadeState != null ? facadeState : originalState;
    }
}
