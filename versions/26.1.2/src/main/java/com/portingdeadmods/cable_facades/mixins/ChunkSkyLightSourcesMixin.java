package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

    @WrapOperation(
            method = "updateEdge",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/ChunkSkyLightSources;isEdgeOccluded(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean cable_facades$facadeAwareUpdateEdge(BlockState topState, BlockState bottomState,
                                                       Operation<Boolean> original,
                                                       @Local(argsOnly = true) BlockGetter level,
                                                       @Local(argsOnly = true, ordinal = 0) BlockPos topPos,
                                                       @Local(argsOnly = true, ordinal = 1) BlockPos bottomPos) {
        return cable_facades$isEdgeOccluded(level, topPos, topState, bottomPos, bottomState);
    }

    @WrapOperation(
            method = "findLowestSourceBelow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/lighting/ChunkSkyLightSources;isEdgeOccluded(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    )
    private boolean cable_facades$facadeAwareFindLowestSourceBelow(BlockState topState, BlockState bottomState,
                                                                  Operation<Boolean> original,
                                                                  @Local(argsOnly = true) BlockGetter level,
                                                                  @Local(ordinal = 0) BlockPos.MutableBlockPos topPos,
                                                                  @Local(ordinal = 1) BlockPos.MutableBlockPos bottomPos) {
        return cable_facades$isEdgeOccluded(level, topPos, topState, bottomPos, bottomState);
    }

    @Unique
    private static boolean cable_facades$isEdgeOccluded(BlockGetter level, BlockPos topPos, BlockState topState,
                                                       BlockPos bottomPos, BlockState bottomState) {
        BlockState facadeTopState = cable_facades$facadeFaceOrOriginal(level, topPos, Direction.DOWN, topState);
        BlockState facadeBottomState = cable_facades$facadeFaceOrOriginal(level, bottomPos, Direction.UP, bottomState);
        if (facadeBottomState.getLightDampening() != 0) {
            return true;
        }

        VoxelShape topShape = LightEngine.getOcclusionShape(facadeTopState, Direction.DOWN);
        VoxelShape bottomShape = LightEngine.getOcclusionShape(facadeBottomState, Direction.UP);
        return Shapes.faceShapeOccludes(topShape, bottomShape);
    }

    @Unique
    private static BlockState cable_facades$facadeFaceOrOriginal(BlockGetter level, BlockPos pos, Direction face, BlockState originalState) {
        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data == null) {
            return originalState;
        }
        BlockState facadeState = data.getFace(face);
        return facadeState != null ? facadeState : originalState;
    }
}
