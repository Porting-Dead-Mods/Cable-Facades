package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.compat.iris.IrisUtil;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;
import java.util.function.Function;

public final class FacadeChunkRenderer {

    private static final long FACADE_RENDER_SEED = 42L;
    private static final float ZFIGHTING_SCALE = 0.99995F;
    private static final float SCALE_UP_FACTOR = 1.0005F;
    private static final float COVER_THICKNESS = 1.0F / 16.0F;
    private static final float[] COVER_SOFT_BOUNDS = {0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F};
    private static final int[] COVER_AXIS_BY_SIDE = {1, 1, 2, 2, 0, 0};
    private static final AABB[] COVER_BOXES = {
            new AABB(0.0, 0.0, 0.0, 1.0, COVER_THICKNESS, 1.0),
            new AABB(0.0, 1.0 - COVER_THICKNESS, 0.0, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, 1.0, 1.0, COVER_THICKNESS),
            new AABB(0.0, 0.0, 1.0 - COVER_THICKNESS, 1.0, 1.0, 1.0),
            new AABB(0.0, 0.0, 0.0, COVER_THICKNESS, 1.0, 1.0),
            new AABB(1.0 - COVER_THICKNESS, 0.0, 0.0, 1.0, 1.0, 1.0)
    };

    private FacadeChunkRenderer() {}

    public static void renderFullBlock(AddSectionGeometryEvent.SectionRenderingContext ctx,
                                       BlockAndTintGetter level, BlockPos pos, BlockState facadeState) {
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet modelSet = mc.getModelManager().getBlockStateModelSet();
        BlockStateModel model = modelSet.get(facadeState);
        ModelBlockRenderer blockRenderer = ctx.getBlockRenderer();

        Block facadedBlock = level.getBlockState(pos).getBlock();
        float scale = computeScale(facadedBlock);

        float baseX = SectionPos.sectionRelative(pos.getX());
        float baseY = SectionPos.sectionRelative(pos.getY());
        float baseZ = SectionPos.sectionRelative(pos.getZ());

        IrisAwareBufferLookup bufferLookup = new IrisAwareBufferLookup(ctx, facadeState, pos);
        try {
            BlockQuadOutput output = scale == 1.0F
                    ? plainOutput(bufferLookup)
                    : scalingOutput(bufferLookup, scale);

            blockRenderer.tesselateBlock(output, baseX, baseY, baseZ, level, pos, facadeState, model, facadeState.getSeed(pos));
        } finally {
            bufferLookup.endBlocks();
        }
    }

    public static void renderDirectional(AddSectionGeometryEvent.SectionRenderingContext ctx,
                                         BlockAndTintGetter level, BlockPos pos, Direction face, BlockState facadeState) {
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet modelSet = mc.getModelManager().getBlockStateModelSet();
        BlockStateModel model = modelSet.get(facadeState);
        ModelBlockRenderer blockRenderer = ctx.getBlockRenderer();

        AABB bounds = COVER_BOXES[face.ordinal()];
        int coverAxis = COVER_AXIS_BY_SIDE[face.ordinal()];
        Block facadedBlock = level.getBlockState(pos).getBlock();
        float scale = computeScale(facadedBlock);

        float baseX = SectionPos.sectionRelative(pos.getX());
        float baseY = SectionPos.sectionRelative(pos.getY());
        float baseZ = SectionPos.sectionRelative(pos.getZ());

        IrisAwareBufferLookup bufferLookup = new IrisAwareBufferLookup(ctx, facadeState, pos);
        try {
            BlockQuadOutput output = slicingOutput(bufferLookup, face, bounds, coverAxis, scale);
            blockRenderer.tesselateBlock(output, baseX, baseY, baseZ, level, pos, facadeState, model, facadeState.getSeed(pos));
        } finally {
            bufferLookup.endBlocks();
        }
    }

    private static float computeScale(Block facadedBlock) {
        float scale = 1.0F;
        if (CFConfig.canPatchZFighting(facadedBlock)) {
            scale *= ZFIGHTING_SCALE;
        }
        if (CFConfig.isScaleUpBlock(facadedBlock)) {
            scale *= SCALE_UP_FACTOR;
        }
        return scale;
    }

    private static BlockQuadOutput plainOutput(Function<ChunkSectionLayer, VertexConsumer> bufferLookup) {
        return (x, y, z, quad, instance) -> {
            VertexConsumer buffer = bufferLookup.apply(quad.materialInfo().layer());
            buffer.putBlockBakedQuad(x, y, z, quad, instance);
        };
    }

    private static BlockQuadOutput scalingOutput(Function<ChunkSectionLayer, VertexConsumer> bufferLookup, float scale) {
        return (x, y, z, quad, instance) -> {
            VertexConsumer buffer = bufferLookup.apply(quad.materialInfo().layer());
            emitScaledQuad(buffer, x, y, z, quad, instance, scale);
        };
    }

    private static BlockQuadOutput slicingOutput(Function<ChunkSectionLayer, VertexConsumer> bufferLookup,
                                                 Direction face, AABB bounds, int coverAxis, float scale) {
        return (x, y, z, quad, instance) -> {
            if (quad.direction() != face) {
                return;
            }
            VertexConsumer buffer = bufferLookup.apply(quad.materialInfo().layer());
            emitSlicedQuad(buffer, x, y, z, quad, instance, bounds, coverAxis, scale);
        };
    }

    private static void emitScaledQuad(VertexConsumer buffer, float x, float y, float z,
                                       BakedQuad quad, QuadInstance instance, float scale) {
        Vector3fc normal = quad.direction().getUnitVec3f();
        int lightEmission = quad.materialInfo().lightEmission();
        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3fc pos = quad.position(vertex);
            float vx = (pos.x() - 0.5F) * scale + 0.5F;
            float vy = (pos.y() - 0.5F) * scale + 0.5F;
            float vz = (pos.z() - 0.5F) * scale + 0.5F;
            long packedUv = quad.packedUV(vertex);
            buffer.addVertex(
                    vx + x, vy + y, vz + z,
                    instance.getColor(vertex),
                    UVPair.unpackU(packedUv),
                    UVPair.unpackV(packedUv),
                    instance.overlayCoords(),
                    instance.getLightCoordsWithEmission(vertex, lightEmission),
                    normal.x(), normal.y(), normal.z()
            );
        }
    }

    private static void emitSlicedQuad(VertexConsumer buffer, float x, float y, float z,
                                       BakedQuad quad, QuadInstance instance,
                                       AABB bounds, int coverAxis, float scale) {
        Vector3fc normal = quad.direction().getUnitVec3f();
        int lightEmission = quad.materialInfo().lightEmission();
        Vector3f tmp = new Vector3f();
        for (int vertex = 0; vertex < 4; vertex++) {
            Vector3fc pos = quad.position(vertex);
            tmp.set(pos);
            clampAxisToCover(tmp, coverAxis, bounds);
            if (scale != 1.0F) {
                tmp.set(
                        (tmp.x() - 0.5F) * scale + 0.5F,
                        (tmp.y() - 0.5F) * scale + 0.5F,
                        (tmp.z() - 0.5F) * scale + 0.5F
                );
            }
            long packedUv = quad.packedUV(vertex);
            buffer.addVertex(
                    tmp.x() + x, tmp.y() + y, tmp.z() + z,
                    instance.getColor(vertex),
                    UVPair.unpackU(packedUv),
                    UVPair.unpackV(packedUv),
                    instance.overlayCoords(),
                    instance.getLightCoordsWithEmission(vertex, lightEmission),
                    normal.x(), normal.y(), normal.z()
            );
        }
    }

    private static void clampAxisToCover(Vector3f v, int axis, AABB bounds) {
        switch (axis) {
            case 0 -> v.x = (float) clamp(v.x(), bounds.minX, bounds.maxX);
            case 1 -> v.y = (float) clamp(v.y(), bounds.minY, bounds.maxY);
            case 2 -> v.z = (float) clamp(v.z(), bounds.minZ, bounds.maxZ);
        }
    }

    private static double clamp(float value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static final class IrisAwareBufferLookup implements Function<ChunkSectionLayer, VertexConsumer> {
        private final AddSectionGeometryEvent.SectionRenderingContext ctx;
        private final BlockState facadeState;
        private final BlockPos pos;
        private final Set<VertexConsumer> touched;
        private final boolean irisActive;

        IrisAwareBufferLookup(AddSectionGeometryEvent.SectionRenderingContext ctx, BlockState facadeState, BlockPos pos) {
            this.ctx = ctx;
            this.facadeState = facadeState;
            this.pos = pos;
            this.irisActive = CFMain.isIrisLoaded();
            this.touched = this.irisActive ? new ReferenceOpenHashSet<>() : null;
        }

        @Override
        public VertexConsumer apply(ChunkSectionLayer layer) {
            VertexConsumer buffer = ctx.getOrCreateChunkBuffer(layer);
            if (irisActive && touched.add(buffer)) {
                IrisUtil.beginBlock(buffer, facadeState, pos);
            }
            return buffer;
        }

        void endBlocks() {
            if (!irisActive) return;
            for (VertexConsumer buffer : touched) {
                IrisUtil.endBlock(buffer);
            }
            touched.clear();
        }
    }
}
