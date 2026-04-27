package com.portingdeadmods.cable_facades.client.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.IModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class CoverQuadRenderer {

    private static final float COVER_THICKNESS = 1.0F / 16.0F;
    private static final int[] COVER_AXIS_BY_SIDE = {1, 1, 2, 2, 0, 0};
    private static final float[] COVER_SOFT_BOUNDS = {0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F};
    private static final float COVER_EDGE_CLAMP = 1.0F / 512.0F;
    private static final float FLOAT_EPSILON = 1.0E-6F;
    private static final int VERTEX_STRIDE = 8;
    private static final int TEXTURE_U = 4;
    private static final int TEXTURE_V = 5;
    private static final int NORMAL = 7;

    private static final AABB[] COVER_BOXES = {
            new AABB(0.0, 0.0, 0.0, 1.0, COVER_THICKNESS, 1.0),
            new AABB(0.0, 1.0 - COVER_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, 1.0, 1.0, COVER_THICKNESS),
            new AABB(0.0, 0.0, 1.0 - COVER_THICKNESS, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, COVER_THICKNESS, 1.0, 1.0),
            new AABB(1.0 - COVER_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0)
    };

    private CoverQuadRenderer() {}

    public static List<BakedQuad> sliceQuads(BlockState state, BlockPos pos, BakedModel model,
                                             Direction side, IModelData modelData) {
        long seed = state.getSeed(pos);
        AABB bounds = COVER_BOXES[side.ordinal()];
        List<BakedQuad> sourceQuads = new ArrayList<>();

        sourceQuads.addAll(model.getQuads(state, null, new Random(seed), modelData));
        for (Direction face : Direction.values()) {
            sourceQuads.addAll(model.getQuads(state, face, new Random(seed), modelData));
        }

        List<BakedQuad> result = new ArrayList<>(sourceQuads.size());
        for (BakedQuad sourceQuad : sourceQuads) {
            result.add(sliceQuad(sourceQuad, side, bounds));
        }
        return result;
    }

    private static BakedQuad sliceQuad(BakedQuad sourceQuad, Direction side, AABB bounds) {
        MutableQuad quad = new MutableQuad(sourceQuad);
        Direction originalDirection = sourceQuad.getDirection();
        int sideIndex = side.ordinal();
        int coverAxis = COVER_AXIS_BY_SIDE[sideIndex];
        float softBound = COVER_SOFT_BOUNDS[sideIndex];
        float oppositeBound = 1.0F - softBound;

        boolean differentFromNear = false;
        boolean differentFromFar = false;
        boolean[] flat = {true, true, true};
        float[][] positions = new float[4][3];
        float[] first = new float[3];

        for (int vertex = 0; vertex < 4; vertex++) {
            positions[vertex][0] = quad.getPosition(vertex, 0);
            positions[vertex][1] = quad.getPosition(vertex, 1);
            positions[vertex][2] = quad.getPosition(vertex, 2);

            if (vertex == 0) {
                first[0] = positions[vertex][0];
                first[1] = positions[vertex][1];
                first[2] = positions[vertex][2];
            } else {
                flat[0] &= sameValue(positions[vertex][0], first[0]);
                flat[1] &= sameValue(positions[vertex][1], first[1]);
                flat[2] &= sameValue(positions[vertex][2], first[2]);
            }

            differentFromNear |= !sameValue(positions[vertex][coverAxis], softBound);
            differentFromFar |= !sameValue(positions[vertex][coverAxis], oppositeBound);
        }

        int slicedAxis = -1;
        boolean clampVisibleArea = differentFromNear && differentFromFar;
        if (clampVisibleArea) {
            for (int axis = 0; axis < 3; axis++) {
                if (!flat[axis]) continue;
                if (axis != coverAxis) {
                    slicedAxis = axis;
                    break;
                }
                clampVisibleArea = false;
            }
        }

        for (int vertex = 0; vertex < 4; vertex++) {
            boolean offFace = !sameValue(positions[vertex][coverAxis], softBound);

            for (int axis = 0; axis < 3; axis++) {
                if (axis == coverAxis) {
                    positions[vertex][axis] = clampToCoverBounds(positions[vertex][axis], bounds, axis);
                } else if (clampVisibleArea && offFace) {
                    positions[vertex][axis] = clampToVisibleArea(positions[vertex][axis]);
                }
            }

            quad.setPosition(vertex, 0, positions[vertex][0]);
            quad.setPosition(vertex, 1, positions[vertex][1]);
            quad.setPosition(vertex, 2, positions[vertex][2]);

            if (slicedAxis != -1) {
                float[] uv = remapFaceUv(originalDirection, positions[vertex][0], positions[vertex][1], positions[vertex][2]);
                quad.setUv(vertex, quad.sprite.getU(clamp01(uv[0]) * 16.0F), quad.sprite.getV(clamp01(uv[1]) * 16.0F));
            }
        }

        quad.recalculateOrientationAndNormal();
        return quad.bake();
    }

    private static boolean sameValue(float a, float b) {
        return Math.abs(a - b) <= FLOAT_EPSILON;
    }

    private static float clampToCoverBounds(float value, AABB bounds, int axis) {
        double min = getMin(bounds, axis);
        double max = getMax(bounds, axis);
        if (value < min) {
            return (float) (min - (min - value) * COVER_EDGE_CLAMP);
        }
        if (value > max) {
            return (float) (max + (value - max) * COVER_EDGE_CLAMP);
        }
        return value;
    }

    private static float clampToVisibleArea(float value) {
        return Math.max(COVER_EDGE_CLAMP, Math.min(1.0F - COVER_EDGE_CLAMP, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float[] remapFaceUv(Direction direction, float x, float y, float z) {
        return switch (direction) {
            case DOWN -> new float[]{x, 1.0F - z};
            case UP -> new float[]{x, z};
            case NORTH -> new float[]{1.0F - x, 1.0F - y};
            case SOUTH -> new float[]{x, 1.0F - y};
            case WEST -> new float[]{z, 1.0F - y};
            case EAST -> new float[]{1.0F - z, 1.0F - y};
        };
    }

    private static double getMin(AABB bounds, int axis) {
        return switch (axis) {
            case 0 -> bounds.minX;
            case 1 -> bounds.minY;
            case 2 -> bounds.minZ;
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        };
    }

    private static double getMax(AABB bounds, int axis) {
        return switch (axis) {
            case 0 -> bounds.maxX;
            case 1 -> bounds.maxY;
            case 2 -> bounds.maxZ;
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        };
    }

    private static int packNormal(float x, float y, float z) {
        int packedX = packNormalComponent(x);
        int packedY = packNormalComponent(y);
        int packedZ = packNormalComponent(z);
        return packedX | (packedY << 8) | (packedZ << 16);
    }

    private static int packNormalComponent(float value) {
        int packed = Math.round(Math.max(-1.0F, Math.min(1.0F, value)) * 127.0F);
        return packed & 0xFF;
    }

    private static final class MutableQuad {
        private final int[] vertices;
        private int tintIndex;
        private Direction direction;
        private final TextureAtlasSprite sprite;
        private final boolean shade;

        private MutableQuad(BakedQuad quad) {
            this.vertices = quad.getVertices().clone();
            this.tintIndex = quad.getTintIndex();
            this.direction = quad.getDirection();
            this.sprite = quad.getSprite();
            this.shade = quad.isShade();
        }

        private float getPosition(int vertex, int axis) {
            return Float.intBitsToFloat(vertices[vertex * VERTEX_STRIDE + axis]);
        }

        private void setPosition(int vertex, int axis, float value) {
            vertices[vertex * VERTEX_STRIDE + axis] = Float.floatToRawIntBits(value);
        }

        private void setUv(int vertex, float u, float v) {
            int baseIndex = vertex * VERTEX_STRIDE;
            vertices[baseIndex + TEXTURE_U] = Float.floatToRawIntBits(u);
            vertices[baseIndex + TEXTURE_V] = Float.floatToRawIntBits(v);
        }

        private void recalculateOrientationAndNormal() {
            float x0 = getPosition(0, 0); float y0 = getPosition(0, 1); float z0 = getPosition(0, 2);
            float x1 = getPosition(1, 0); float y1 = getPosition(1, 1); float z1 = getPosition(1, 2);
            float x2 = getPosition(2, 0); float y2 = getPosition(2, 1); float z2 = getPosition(2, 2);
            float x3 = getPosition(3, 0); float y3 = getPosition(3, 1); float z3 = getPosition(3, 2);

            float v1x = x3 - x1; float v1y = y3 - y1; float v1z = z3 - z1;
            float v2x = x2 - x0; float v2y = y2 - y0; float v2z = z2 - z0;

            float normalX = v2y * v1z - v2z * v1y;
            float normalY = v2z * v1x - v2x * v1z;
            float normalZ = v2x * v1y - v2y * v1x;
            float lengthSquared = normalX * normalX + normalY * normalY + normalZ * normalZ;
            if (lengthSquared <= FLOAT_EPSILON) return;

            float inverseLength = (float) (1.0D / Math.sqrt(lengthSquared));
            normalX *= inverseLength; normalY *= inverseLength; normalZ *= inverseLength;
            direction = Direction.getNearest(normalX, normalY, normalZ);

            int packedNormal = packNormal(normalX, normalY, normalZ);
            for (int vertex = 0; vertex < 4; vertex++) {
                vertices[vertex * VERTEX_STRIDE + NORMAL] = packedNormal;
            }
        }

        private BakedQuad bake() {
            return new BakedQuad(vertices, tintIndex, direction, sprite, shade);
        }
    }
}
