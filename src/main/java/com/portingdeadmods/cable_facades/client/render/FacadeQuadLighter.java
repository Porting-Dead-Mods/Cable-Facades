package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.BitSet;

public final class FacadeQuadLighter {

    private static final Direction[] DIRECTIONS = Direction.values();

    private FacadeQuadLighter() {
    }

    public static void renderQuad(BlockAndTintGetter level, BlockState state, BlockPos pos,
                                  PoseStack.Pose pose, VertexConsumer buffer, BakedQuad quad,
                                  float red, float green, float blue, int overlay, boolean useAo) {
        if (useAo) {
            float[] shape = new float[DIRECTIONS.length * 2];
            BitSet flags = new BitSet(2);
            calculateShape(level, state, pos, quad.getVertices(), quad.getDirection(), shape, flags);

            AmbientOcclusionFace aoFace = new AmbientOcclusionFace();
            aoFace.calculate(level, state, pos, quad.getDirection(), shape, flags, quad.isShade());
            buffer.putBulkData(pose, quad, aoFace.brightness, red, green, blue, 1.0F, aoFace.lightmap, overlay, true);
            return;
        }

        BitSet flags = new BitSet(2);
        calculateShape(level, state, pos, quad.getVertices(), quad.getDirection(), null, flags);
        BlockPos lightPos = flags.get(0) ? pos.relative(quad.getDirection()) : pos;
        int light = LevelRenderer.getLightColor(level, state, lightPos);
        float shade = level.getShade(quad.getDirection(), quad.isShade());
        buffer.putBulkData(pose, quad, new float[]{shade, shade, shade, shade}, red, green, blue, 1.0F,
                new int[]{light, light, light, light}, overlay, true);
    }

    private static void calculateShape(BlockAndTintGetter level, BlockState state, BlockPos pos, int[] vertices,
                                       Direction direction, float[] shape, BitSet flags) {
        float minX = 32.0F;
        float minY = 32.0F;
        float minZ = 32.0F;
        float maxX = -32.0F;
        float maxY = -32.0F;
        float maxZ = -32.0F;

        for (int i = 0; i < 4; i++) {
            float x = Float.intBitsToFloat(vertices[i * 8]);
            float y = Float.intBitsToFloat(vertices[i * 8 + 1]);
            float z = Float.intBitsToFloat(vertices[i * 8 + 2]);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        if (shape != null) {
            shape[Direction.WEST.get3DDataValue()] = minX;
            shape[Direction.EAST.get3DDataValue()] = maxX;
            shape[Direction.DOWN.get3DDataValue()] = minY;
            shape[Direction.UP.get3DDataValue()] = maxY;
            shape[Direction.NORTH.get3DDataValue()] = minZ;
            shape[Direction.SOUTH.get3DDataValue()] = maxZ;
            int offset = DIRECTIONS.length;
            shape[Direction.WEST.get3DDataValue() + offset] = 1.0F - minX;
            shape[Direction.EAST.get3DDataValue() + offset] = 1.0F - maxX;
            shape[Direction.DOWN.get3DDataValue() + offset] = 1.0F - minY;
            shape[Direction.UP.get3DDataValue() + offset] = 1.0F - maxY;
            shape[Direction.NORTH.get3DDataValue() + offset] = 1.0F - minZ;
            shape[Direction.SOUTH.get3DDataValue() + offset] = 1.0F - maxZ;
        }

        float epsilon = 1.0E-4F;
        float almostOne = 0.9999F;
        switch (direction) {
            case DOWN -> {
                flags.set(1, minX >= epsilon || minZ >= epsilon || maxX <= almostOne || maxZ <= almostOne);
                flags.set(0, minY == maxY && (minY < epsilon || state.isCollisionShapeFullBlock(level, pos)));
            }
            case UP -> {
                flags.set(1, minX >= epsilon || minZ >= epsilon || maxX <= almostOne || maxZ <= almostOne);
                flags.set(0, minY == maxY && (maxY > almostOne || state.isCollisionShapeFullBlock(level, pos)));
            }
            case NORTH -> {
                flags.set(1, minX >= epsilon || minY >= epsilon || maxX <= almostOne || maxY <= almostOne);
                flags.set(0, minZ == maxZ && (minZ < epsilon || state.isCollisionShapeFullBlock(level, pos)));
            }
            case SOUTH -> {
                flags.set(1, minX >= epsilon || minY >= epsilon || maxX <= almostOne || maxY <= almostOne);
                flags.set(0, minZ == maxZ && (maxZ > almostOne || state.isCollisionShapeFullBlock(level, pos)));
            }
            case WEST -> {
                flags.set(1, minY >= epsilon || minZ >= epsilon || maxY <= almostOne || maxZ <= almostOne);
                flags.set(0, minX == maxX && (minX < epsilon || state.isCollisionShapeFullBlock(level, pos)));
            }
            case EAST -> {
                flags.set(1, minY >= epsilon || minZ >= epsilon || maxY <= almostOne || maxZ <= almostOne);
                flags.set(0, minX == maxX && (maxX > almostOne || state.isCollisionShapeFullBlock(level, pos)));
            }
        }
    }

    private static final class AmbientOcclusionFace {
        private final float[] brightness = new float[4];
        private final int[] lightmap = new int[4];

        private void calculate(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction,
                               float[] shape, BitSet flags, boolean shade) {
            BlockPos basePos = flags.get(0) ? pos.relative(direction) : pos;
            AdjacencyInfo adjacency = AdjacencyInfo.fromFacing(direction);
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            cursor.setWithOffset(basePos, adjacency.corners[0]);
            BlockState state0 = level.getBlockState(cursor);
            int light0 = LevelRenderer.getLightColor(level, state0, cursor);
            float bright0 = state0.getShadeBrightness(level, cursor);

            cursor.setWithOffset(basePos, adjacency.corners[1]);
            BlockState state1 = level.getBlockState(cursor);
            int light1 = LevelRenderer.getLightColor(level, state1, cursor);
            float bright1 = state1.getShadeBrightness(level, cursor);

            cursor.setWithOffset(basePos, adjacency.corners[2]);
            BlockState state2 = level.getBlockState(cursor);
            int light2 = LevelRenderer.getLightColor(level, state2, cursor);
            float bright2 = state2.getShadeBrightness(level, cursor);

            cursor.setWithOffset(basePos, adjacency.corners[3]);
            BlockState state3 = level.getBlockState(cursor);
            int light3 = LevelRenderer.getLightColor(level, state3, cursor);
            float bright3 = state3.getShadeBrightness(level, cursor);

            BlockState edge0 = level.getBlockState(cursor.setWithOffset(basePos, adjacency.corners[0]).move(direction));
            boolean open0 = !edge0.isViewBlocking(level, cursor) || edge0.getLightBlock(level, cursor) == 0;
            BlockState edge1 = level.getBlockState(cursor.setWithOffset(basePos, adjacency.corners[1]).move(direction));
            boolean open1 = !edge1.isViewBlocking(level, cursor) || edge1.getLightBlock(level, cursor) == 0;
            BlockState edge2 = level.getBlockState(cursor.setWithOffset(basePos, adjacency.corners[2]).move(direction));
            boolean open2 = !edge2.isViewBlocking(level, cursor) || edge2.getLightBlock(level, cursor) == 0;
            BlockState edge3 = level.getBlockState(cursor.setWithOffset(basePos, adjacency.corners[3]).move(direction));
            boolean open3 = !edge3.isViewBlocking(level, cursor) || edge3.getLightBlock(level, cursor) == 0;

            float cornerBrightness0;
            int cornerLight0;
            if (!open2 && !open0) {
                cornerBrightness0 = bright0;
                cornerLight0 = light0;
            } else {
                cursor.setWithOffset(basePos, adjacency.corners[0]).move(adjacency.corners[2]);
                BlockState corner = level.getBlockState(cursor);
                cornerBrightness0 = corner.getShadeBrightness(level, cursor);
                cornerLight0 = LevelRenderer.getLightColor(level, corner, cursor);
            }

            float cornerBrightness1;
            int cornerLight1;
            if (!open3 && !open0) {
                cornerBrightness1 = bright0;
                cornerLight1 = light0;
            } else {
                cursor.setWithOffset(basePos, adjacency.corners[0]).move(adjacency.corners[3]);
                BlockState corner = level.getBlockState(cursor);
                cornerBrightness1 = corner.getShadeBrightness(level, cursor);
                cornerLight1 = LevelRenderer.getLightColor(level, corner, cursor);
            }

            float cornerBrightness2;
            int cornerLight2;
            if (!open2 && !open1) {
                cornerBrightness2 = bright0;
                cornerLight2 = light0;
            } else {
                cursor.setWithOffset(basePos, adjacency.corners[1]).move(adjacency.corners[2]);
                BlockState corner = level.getBlockState(cursor);
                cornerBrightness2 = corner.getShadeBrightness(level, cursor);
                cornerLight2 = LevelRenderer.getLightColor(level, corner, cursor);
            }

            float cornerBrightness3;
            int cornerLight3;
            if (!open3 && !open1) {
                cornerBrightness3 = bright0;
                cornerLight3 = light0;
            } else {
                cursor.setWithOffset(basePos, adjacency.corners[1]).move(adjacency.corners[3]);
                BlockState corner = level.getBlockState(cursor);
                cornerBrightness3 = corner.getShadeBrightness(level, cursor);
                cornerLight3 = LevelRenderer.getLightColor(level, corner, cursor);
            }

            int selfLight = LevelRenderer.getLightColor(level, state, pos);
            cursor.setWithOffset(pos, direction);
            BlockState frontState = level.getBlockState(cursor);
            if (flags.get(0) || !frontState.isSolidRender(level, cursor)) {
                selfLight = LevelRenderer.getLightColor(level, frontState, cursor);
            }

            float selfBrightness = flags.get(0)
                    ? level.getBlockState(basePos).getShadeBrightness(level, basePos)
                    : state.getShadeBrightness(level, pos);

            AmbientVertexRemap remap = AmbientVertexRemap.fromFacing(direction);
            if (flags.get(1) && adjacency.doNonCubicWeight) {
                float blend0 = (bright3 + bright0 + cornerBrightness1 + selfBrightness) * 0.25F;
                float blend1 = (bright2 + bright0 + cornerBrightness0 + selfBrightness) * 0.25F;
                float blend2 = (bright2 + bright1 + cornerBrightness2 + selfBrightness) * 0.25F;
                float blend3 = (bright3 + bright1 + cornerBrightness3 + selfBrightness) * 0.25F;

                float w00 = shape[adjacency.vert0Weights[0].shape] * shape[adjacency.vert0Weights[1].shape];
                float w01 = shape[adjacency.vert0Weights[2].shape] * shape[adjacency.vert0Weights[3].shape];
                float w02 = shape[adjacency.vert0Weights[4].shape] * shape[adjacency.vert0Weights[5].shape];
                float w03 = shape[adjacency.vert0Weights[6].shape] * shape[adjacency.vert0Weights[7].shape];
                float w10 = shape[adjacency.vert1Weights[0].shape] * shape[adjacency.vert1Weights[1].shape];
                float w11 = shape[adjacency.vert1Weights[2].shape] * shape[adjacency.vert1Weights[3].shape];
                float w12 = shape[adjacency.vert1Weights[4].shape] * shape[adjacency.vert1Weights[5].shape];
                float w13 = shape[adjacency.vert1Weights[6].shape] * shape[adjacency.vert1Weights[7].shape];
                float w20 = shape[adjacency.vert2Weights[0].shape] * shape[adjacency.vert2Weights[1].shape];
                float w21 = shape[adjacency.vert2Weights[2].shape] * shape[adjacency.vert2Weights[3].shape];
                float w22 = shape[adjacency.vert2Weights[4].shape] * shape[adjacency.vert2Weights[5].shape];
                float w23 = shape[adjacency.vert2Weights[6].shape] * shape[adjacency.vert2Weights[7].shape];
                float w30 = shape[adjacency.vert3Weights[0].shape] * shape[adjacency.vert3Weights[1].shape];
                float w31 = shape[adjacency.vert3Weights[2].shape] * shape[adjacency.vert3Weights[3].shape];
                float w32 = shape[adjacency.vert3Weights[4].shape] * shape[adjacency.vert3Weights[5].shape];
                float w33 = shape[adjacency.vert3Weights[6].shape] * shape[adjacency.vert3Weights[7].shape];

                brightness[remap.vert0] = blend0 * w00 + blend1 * w01 + blend2 * w02 + blend3 * w03;
                brightness[remap.vert1] = blend0 * w10 + blend1 * w11 + blend2 * w12 + blend3 * w13;
                brightness[remap.vert2] = blend0 * w20 + blend1 * w21 + blend2 * w22 + blend3 * w23;
                brightness[remap.vert3] = blend0 * w30 + blend1 * w31 + blend2 * w32 + blend3 * w33;

                int mix0 = blendLight(light3, light0, cornerLight1, selfLight);
                int mix1 = blendLight(light2, light0, cornerLight0, selfLight);
                int mix2 = blendLight(light2, light1, cornerLight2, selfLight);
                int mix3 = blendLight(light3, light1, cornerLight3, selfLight);
                lightmap[remap.vert0] = blendLightWeighted(mix0, mix1, mix2, mix3, w00, w01, w02, w03);
                lightmap[remap.vert1] = blendLightWeighted(mix0, mix1, mix2, mix3, w10, w11, w12, w13);
                lightmap[remap.vert2] = blendLightWeighted(mix0, mix1, mix2, mix3, w20, w21, w22, w23);
                lightmap[remap.vert3] = blendLightWeighted(mix0, mix1, mix2, mix3, w30, w31, w32, w33);
            } else {
                brightness[remap.vert0] = (bright3 + bright0 + cornerBrightness1 + selfBrightness) * 0.25F;
                brightness[remap.vert1] = (bright2 + bright0 + cornerBrightness0 + selfBrightness) * 0.25F;
                brightness[remap.vert2] = (bright2 + bright1 + cornerBrightness2 + selfBrightness) * 0.25F;
                brightness[remap.vert3] = (bright3 + bright1 + cornerBrightness3 + selfBrightness) * 0.25F;
                lightmap[remap.vert0] = blendLight(light3, light0, cornerLight1, selfLight);
                lightmap[remap.vert1] = blendLight(light2, light0, cornerLight0, selfLight);
                lightmap[remap.vert2] = blendLight(light2, light1, cornerLight2, selfLight);
                lightmap[remap.vert3] = blendLight(light3, light1, cornerLight3, selfLight);
            }

            float faceShade = level.getShade(direction, shade);
            for (int i = 0; i < brightness.length; i++) {
                brightness[i] *= faceShade;
            }
        }

        private static int blendLight(int a, int b, int c, int d) {
            if (a == 0) {
                a = d;
            }
            if (b == 0) {
                b = d;
            }
            if (c == 0) {
                c = d;
            }
            return a + b + c + d >> 2 & 0xFF00FF;
        }

        private static int blendLightWeighted(int a, int b, int c, int d, float wa, float wb, float wc, float wd) {
            int sky = (int) ((a >> 16 & 0xFF) * wa + (b >> 16 & 0xFF) * wb + (c >> 16 & 0xFF) * wc + (d >> 16 & 0xFF) * wd) & 0xFF;
            int block = (int) ((a & 0xFF) * wa + (b & 0xFF) * wb + (c & 0xFF) * wc + (d & 0xFF) * wd) & 0xFF;
            return sky << 16 | block;
        }
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
        private static final AmbientVertexRemap[] BY_FACING = Util.make(new AmbientVertexRemap[6], remaps -> {
            remaps[Direction.DOWN.get3DDataValue()] = DOWN;
            remaps[Direction.UP.get3DDataValue()] = UP;
            remaps[Direction.NORTH.get3DDataValue()] = NORTH;
            remaps[Direction.SOUTH.get3DDataValue()] = SOUTH;
            remaps[Direction.WEST.get3DDataValue()] = WEST;
            remaps[Direction.EAST.get3DDataValue()] = EAST;
        });

        AmbientVertexRemap(int vert0, int vert1, int vert2, int vert3) {
            this.vert0 = vert0;
            this.vert1 = vert1;
            this.vert2 = vert2;
            this.vert3 = vert3;
        }

        private static AmbientVertexRemap fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
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
        private static final AdjacencyInfo[] BY_FACING = Util.make(new AdjacencyInfo[6], infos -> {
            infos[Direction.DOWN.get3DDataValue()] = DOWN;
            infos[Direction.UP.get3DDataValue()] = UP;
            infos[Direction.NORTH.get3DDataValue()] = NORTH;
            infos[Direction.SOUTH.get3DDataValue()] = SOUTH;
            infos[Direction.WEST.get3DDataValue()] = WEST;
            infos[Direction.EAST.get3DDataValue()] = EAST;
        });

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
    }
}
