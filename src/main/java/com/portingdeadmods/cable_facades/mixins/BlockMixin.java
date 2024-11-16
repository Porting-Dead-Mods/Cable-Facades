package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.events.ClientCamoManager;
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

/**
 * Created 16/11/2024 by SuperMartijn642
 */
@Mixin(Block.class)
public abstract class BlockMixin {

    @Unique
    private static final BlockPos FACADE_CHECK_MARKER = new BlockPos(0, 0, 0);

    @ModifyVariable(
        method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("INVOKE_ASSIGN"),
        ordinal = 1
    )
    private static BlockState useFacadeAsNeighbor(BlockState neighbor, BlockState state, BlockGetter level, BlockPos pos, Direction side, BlockPos sidePos){
        if(sidePos == FACADE_CHECK_MARKER){
            Block facade = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos.relative(side));
            if(facade != null)
                return facade.defaultBlockState();
        }
        return neighbor;
    }

    @Inject(
        method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void checkFacadeOcclusion(BlockState state, BlockGetter level, BlockPos pos, Direction side, BlockPos sidePos, CallbackInfoReturnable<Boolean> ci){
        if(pos == FACADE_CHECK_MARKER || !ci.getReturnValue())
            return;

        // Check whether the facade to the side hides the face
        Block facade = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(sidePos);
        if(facade != null)
            ci.setReturnValue(Block.shouldRenderFace(state, level, pos, side, FACADE_CHECK_MARKER));
    }
}
