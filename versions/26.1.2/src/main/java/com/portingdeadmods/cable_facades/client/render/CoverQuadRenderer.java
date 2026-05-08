package com.portingdeadmods.cable_facades.client.render;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public final class CoverQuadRenderer {

    private static final float COVER_THICKNESS = 1.0F / 16.0F;
    private static final int[] COVER_AXIS_BY_SIDE = {1, 1, 2, 2, 0, 0};
    private static final float[] COVER_SOFT_BOUNDS = {0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F};
    private static final float COVER_EDGE_CLAMP = 1.0F / 512.0F;
    private static final float FLOAT_EPSILON = 1.0E-6F;

    private static final AABB[] COVER_BOXES = {
            new AABB(0.0, 0.0, 0.0, 1.0, COVER_THICKNESS, 1.0),
            new AABB(0.0, 1.0 - COVER_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, 1.0, 1.0, COVER_THICKNESS),
            new AABB(0.0, 0.0, 1.0 - COVER_THICKNESS, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, COVER_THICKNESS, 1.0, 1.0),
            new AABB(1.0 - COVER_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0)
    };

    private CoverQuadRenderer() {
    }

    public static List<BakedQuad> sliceQuads(BlockState state, BlockPos pos, BlockStateModel model, Direction side) {
        long seed = state.getSeed(pos);
        AABB bounds = COVER_BOXES[side.ordinal()];

        List<BakedQuad> sourceQuads = new ArrayList<>();
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(seed), parts);
        for (BlockStateModelPart part : parts) {
            sourceQuads.addAll(part.getQuads(null));
            for (Direction face : Direction.values()) {
                sourceQuads.addAll(part.getQuads(face));
            }
        }

        List<BakedQuad> result = new ArrayList<>(sourceQuads.size());
        for (BakedQuad sourceQuad : sourceQuads) {
            result.add(sliceQuad(sourceQuad, side, bounds));
        }
        return result;
    }

    private static BakedQuad sliceQuad(BakedQuad sourceQuad, Direction side, AABB bounds) {
        Direction originalDirection = sourceQuad.direction();
        int sideIndex = side.ordinal();
        int coverAxis = COVER_AXIS_BY_SIDE[sideIndex];
        float softBound = COVER_SOFT_BOUNDS[sideIndex];
        float oppositeBound = 1.0F - softBound;

        Vector3f[] positions = new Vector3f[]{
                new Vector3f(sourceQuad.position0()),
                new Vector3f(sourceQuad.position1()),
                new Vector3f(sourceQuad.position2()),
                new Vector3f(sourceQuad.position3())
        };
        long[] packedUVs = new long[]{
                sourceQuad.packedUV0(),
                sourceQuad.packedUV1(),
                sourceQuad.packedUV2(),
                sourceQuad.packedUV3()
        };

        boolean differentFromNear = false;
        boolean differentFromFar = false;
        boolean[] flat = {true, true, true};
        Vector3f first = positions[0];

        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3f p = positions[vertex];
            if (vertex > 0) {
                flat[0] &= sameValue(p.x(), first.x());
                flat[1] &= sameValue(p.y(), first.y());
                flat[2] &= sameValue(p.z(), first.z());
            }
            differentFromNear |= !sameValue(getAxis(p, coverAxis), softBound);
            differentFromFar |= !sameValue(getAxis(p, coverAxis), oppositeBound);
        }

        int slicedAxis = -1;
        boolean clampVisibleArea = differentFromNear && differentFromFar;
        if (clampVisibleArea) {
            for (int axis = 0; axis < 3; axis++) {
                if (!flat[axis]) {
                    continue;
                }
                if (axis != coverAxis) {
                    slicedAxis = axis;
                    break;
                }
                clampVisibleArea = false;
            }
        }

        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3f p = positions[vertex];
            boolean offFace = !sameValue(getAxis(p, coverAxis), softBound);

            for (int axis = 0; axis < 3; axis++) {
                if (axis == coverAxis) {
                    setAxis(p, axis, clampToCoverBounds(getAxis(p, axis), bounds, axis));
                } else if (clampVisibleArea && offFace) {
                    setAxis(p, axis, clampToVisibleArea(getAxis(p, axis)));
                }
            }

            if (slicedAxis != -1) {
                float[] uv = remapFaceUv(originalDirection, p.x(), p.y(), p.z());
                float u = sourceQuad.materialInfo().sprite().getU(clamp01(uv[0]));
                float v = sourceQuad.materialInfo().sprite().getV(clamp01(uv[1]));
                packedUVs[vertex] = UVPair.pack(u, v);
            }
        }

        Direction newDirection = computeDirection(positions, originalDirection);
        return new BakedQuad(
                positions[0],
                positions[1],
                positions[2],
                positions[3],
                packedUVs[0],
                packedUVs[1],
                packedUVs[2],
                packedUVs[3],
                newDirection,
                sourceQuad.materialInfo()
        );
    }

    private static Direction computeDirection(Vector3f[] positions, Direction fallback) {
        float v1x = positions[3].x() - positions[1].x();
        float v1y = positions[3].y() - positions[1].y();
        float v1z = positions[3].z() - positions[1].z();
        float v2x = positions[2].x() - positions[0].x();
        float v2y = positions[2].y() - positions[0].y();
        float v2z = positions[2].z() - positions[0].z();

        float normalX = v2y * v1z - v2z * v1y;
        float normalY = v2z * v1x - v2x * v1z;
        float normalZ = v2x * v1y - v2y * v1x;
        float lengthSquared = normalX * normalX + normalY * normalY + normalZ * normalZ;
        if (lengthSquared <= FLOAT_EPSILON) {
            return fallback;
        }

        float inverseLength = (float) (1.0D / Math.sqrt(lengthSquared));
        int nx = Math.round(normalX * inverseLength * 127.0F);
        int ny = Math.round(normalY * inverseLength * 127.0F);
        int nz = Math.round(normalZ * inverseLength * 127.0F);
        Direction nearest = Direction.getNearest(nx, ny, nz, fallback);
        return nearest != null ? nearest : fallback;
    }

    private static float getAxis(Vector3fc v, int axis) {
        return switch (axis) {
            case 0 -> v.x();
            case 1 -> v.y();
            case 2 -> v.z();
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        };
    }

    private static void setAxis(Vector3f v, int axis, float value) {
        switch (axis) {
            case 0 -> v.x = value;
            case 1 -> v.y = value;
            case 2 -> v.z = value;
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        }
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
}
