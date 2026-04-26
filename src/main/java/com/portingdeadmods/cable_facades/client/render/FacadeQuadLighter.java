package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class FacadeQuadLighter {

    private static final Direction[] DIRECTIONS = Direction.values();

    private FacadeQuadLighter() {
    }

    public static void renderQuad(BlockAndTintGetter level, ModelBlockRenderer blockRenderer, BlockState state, BlockPos pos,
                                  PoseStack.Pose pose, BlockQuadOutput output, BakedQuad quad,
                                  float x, float y, float z, int overlay, boolean useAo) {
        BlockModelLighter blockModelLighter = ((BlockModelLighterGetter) blockRenderer).cable_facades$getLighter();

        if (useAo) {
            // TODO: Not sure if this is quite right
            QuadInstance outputInstance = new QuadInstance();

            blockModelLighter.prepareQuadAmbientOcclusion(level, state, pos, quad, outputInstance);
            putQuadWithTint(blockRenderer, output, x, y, z, level, state, pos, quad, outputInstance);
        } else {
            // FIXME: Used to be flags.get(0)
            BlockPos lightPos = false ? pos.relative(quad.direction()) : pos;
            QuadInstance outputInstance = new QuadInstance();
            int light = blockModelLighter.getLightCoords(state, level, lightPos);
            blockModelLighter.prepareQuadFlat(level, state, pos, light, quad, outputInstance);

            putQuadWithTint(blockRenderer, output, x, y, z, level, state, pos, quad, outputInstance);
        }
        //calculateShape(level, state, pos, quad.getVertices(), quad.direction(), null, flags);
        //float shade = level.getShade(quad.direction(), quad.materialInfo().shade());
        //buffer.putBulkData(pose, quad, new float[]{shade, shade, shade, shade}, red, green, blue, 1.0F,
        //        new int[]{light, light, light, light}, overlay, true);
    }

    private static void putQuadWithTint(ModelBlockRenderer renderer, BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, QuadInstance quadInstance) {
        int tintIndex = quad.materialInfo().tintIndex();
        if (tintIndex != -1) {
            quadInstance.multiplyColor(renderer.getTintColor(level, state, pos, tintIndex));
        }

        output.put(x, y, z, quad, quadInstance);
    }

    private enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
        private static final AmbientVertexRemap[] BY_FACING;

        AmbientVertexRemap(int vert0, int vert1, int vert2, int vert3) {
            this.vert0 = vert0;
            this.vert1 = vert1;
            this.vert2 = vert2;
            this.vert3 = vert3;
        }

        private static AmbientVertexRemap fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = new AmbientVertexRemap[Direction.values().length];
            BY_FACING[Direction.DOWN.get3DDataValue()] = DOWN;
            BY_FACING[Direction.UP.get3DDataValue()] = UP;
            BY_FACING[Direction.NORTH.get3DDataValue()] = NORTH;
            BY_FACING[Direction.SOUTH.get3DDataValue()] = SOUTH;
            BY_FACING[Direction.WEST.get3DDataValue()] = WEST;
            BY_FACING[Direction.EAST.get3DDataValue()] = EAST;
        }
    }

    private enum SizeInfo {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        private final int shape;

        SizeInfo(Direction direction, boolean flipped) {
            this.shape = direction.get3DDataValue() + (flipped ? DIRECTIONS.length : 0);
        }
    }

    private enum AdjacencyInfo {
        DOWN(
                new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH},
                true,
                new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH},
                new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH}
        ),
        UP(
                new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH},
                true,
                new SizeInfo[]{SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH},
                new SizeInfo[]{SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH}
        ),
        NORTH(
                new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST},
                true,
                new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST},
                new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST},
                new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST},
                new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST}
        ),
        SOUTH(
                new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP},
                true,
                new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST},
                new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST},
                new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST},
                new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST}
        ),
        WEST(
                new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH},
                true,
                new SizeInfo[]{SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH},
                new SizeInfo[]{SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH}
        ),
        EAST(
                new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH},
                true,
                new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH},
                new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH},
                new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH}
        );

        private final Direction[] corners;
        private final boolean doNonCubicWeight;
        private final SizeInfo[] vert0Weights;
        private final SizeInfo[] vert1Weights;
        private final SizeInfo[] vert2Weights;
        private final SizeInfo[] vert3Weights;
        private static final AdjacencyInfo[] BY_FACING;

        AdjacencyInfo(Direction[] corners, boolean doNonCubicWeight, SizeInfo[] vert0Weights, SizeInfo[] vert1Weights,
                      SizeInfo[] vert2Weights, SizeInfo[] vert3Weights) {
            this.corners = corners;
            this.doNonCubicWeight = doNonCubicWeight;
            this.vert0Weights = vert0Weights;
            this.vert1Weights = vert1Weights;
            this.vert2Weights = vert2Weights;
            this.vert3Weights = vert3Weights;
        }

        private static AdjacencyInfo fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = new AdjacencyInfo[Direction.values().length];
            BY_FACING[Direction.DOWN.get3DDataValue()] = DOWN;
            BY_FACING[Direction.UP.get3DDataValue()] = UP;
            BY_FACING[Direction.NORTH.get3DDataValue()] = NORTH;
            BY_FACING[Direction.SOUTH.get3DDataValue()] = SOUTH;
            BY_FACING[Direction.WEST.get3DDataValue()] = WEST;
            BY_FACING[Direction.EAST.get3DDataValue()] = EAST;
        }
    }
}
