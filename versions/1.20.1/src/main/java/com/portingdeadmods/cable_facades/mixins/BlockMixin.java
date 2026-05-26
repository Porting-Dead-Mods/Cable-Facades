package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Unique
    private static final BlockPos FACADE_CHECK_MARKER = new BlockPos(0, 0, 0);

    @ModifyVariable(
            method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
            index = 5
    )
    private static BlockState useFacadeAsNeighbor(BlockState value, BlockState state, BlockGetter level, BlockPos pos, Direction side, BlockPos sidePos) {
        if (pos == null || side == null || sidePos == null) {
            return value;
        }
        if (sidePos.equals(FACADE_CHECK_MARKER)) {
            BlockPos relativePos = pos.relative(side);
            FacadeData data = ClientFacadeManager.get(relativePos);
            if (data != null) {
                if (data.isFullBlock()) {
                    return data.getFullBlock();
                } else if (data.hasFace(side.getOpposite())) {
                    return data.getFace(side.getOpposite());
                }
            }
        }
        return value;
    }

    @Inject(
            method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void checkFacadeOcclusion(BlockState state, BlockGetter level, BlockPos pos, Direction side, BlockPos sidePos, CallbackInfoReturnable<Boolean> ci, @Local(index = 5) BlockState bState) {
        if (state == null || level == null || pos == null || side == null || sidePos == null || ci == null) {
            return;
        }
        if (GameClientEvents.RENDERING_FACADE.get()) {
            FacadeData sideData = ClientFacadeManager.get(sidePos);
            BlockState sideState = null;
            if (sideData != null) {
                if (sideData.isFullBlock()) {
                    sideState = sideData.getFullBlock();
                } else if (sideData.hasFace(side.getOpposite())) {
                    sideState = sideData.getFace(side.getOpposite());
                }
            }
            ci.setReturnValue(sideState != state);
        }
        if (pos.equals(FACADE_CHECK_MARKER) || !ci.getReturnValue()) {
            return;
        }
        try {
            FacadeData data = ClientFacadeManager.get(sidePos);
            if (data != null) {
                boolean shouldRender = safeCheckFaceRendering(state, level, pos, side);
                ci.setReturnValue(shouldRender);
            }
        } catch (Exception e) {
            ci.setReturnValue(ci.getReturnValue());
        }
    }

    @Unique
    private static boolean safeCheckFaceRendering(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        try {
            return Block.shouldRenderFace(state, level, pos, side, FACADE_CHECK_MARKER);
        } catch (ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }
}
