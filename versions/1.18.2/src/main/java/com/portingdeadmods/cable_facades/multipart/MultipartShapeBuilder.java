package com.portingdeadmods.cable_facades.multipart;

import com.portingdeadmods.cable_facades.data.FacadeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;

public final class MultipartShapeBuilder {

    private MultipartShapeBuilder() {}

    public static VoxelShape buildShape(BlockGetter level, BlockPos pos, BlockState blockState,
                                        FacadeData facadeData, CollisionContext ctx) {
        if (facadeData.isFullBlock()) {
            return facadeData.getFullBlock().getShape(level, BlockPos.ZERO, ctx);
        }
        VoxelShape base = blockState.getShape(level, pos, ctx);
        return combine(base, facadeData.directional());
    }

    public static VoxelShape buildCollisionShape(BlockGetter level, BlockPos pos, BlockState blockState,
                                                 FacadeData facadeData, CollisionContext ctx) {
        if (facadeData.isFullBlock()) {
            return facadeData.getFullBlock().getCollisionShape(level, BlockPos.ZERO, ctx);
        }
        VoxelShape base = blockState.getCollisionShape(level, pos, ctx);
        return combine(base, facadeData.directional());
    }

    public static VoxelShape buildVisualShape(BlockGetter level, BlockPos pos,
                                              FacadeData facadeData, CollisionContext ctx) {
        if (facadeData.isFullBlock()) {
            return facadeData.getFullBlock().getVisualShape(level, BlockPos.ZERO, ctx);
        }
        return getCoverOnlyShape(facadeData);
    }

    public static VoxelShape buildOcclusionShape(BlockGetter level, BlockPos pos, FacadeData facadeData) {
        if (facadeData.isFullBlock()) {
            return facadeData.getFullBlock().getOcclusionShape(level, BlockPos.ZERO);
        }
        return Shapes.empty();
    }

    public static VoxelShape getCoverOnlyShape(FacadeData facadeData) {
        if (facadeData.isFullBlock()) {
            return Shapes.block();
        }
        VoxelShape result = Shapes.empty();
        if (facadeData.directional() != null) {
            for (Direction dir : facadeData.directional().keySet()) {
                result = Shapes.or(result, CoverShapes.get(dir));
            }
        }
        return result;
    }

    private static VoxelShape combine(VoxelShape base, EnumMap<Direction, BlockState> covers) {
        if (covers == null || covers.isEmpty()) return base;
        VoxelShape result = base;
        for (Direction dir : covers.keySet()) {
            result = Shapes.or(result, CoverShapes.get(dir));
        }
        return result;
    }
}
