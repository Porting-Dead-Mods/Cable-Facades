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
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import org.joml.Vector3fc;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class FacadeChunkRenderer {

    private static final long FACADE_RENDER_SEED = 42L;
    private static final float ZFIGHTING_SCALE = 0.99995F;
    private static final float SCALE_UP_FACTOR = 1.0005F;

    private FacadeChunkRenderer() {}

    public static void renderFullBlock(AddSectionGeometryEvent.SectionRenderingContext ctx,
                                       BlockAndTintGetter level, BlockPos pos, BlockState facadeState,
                                       boolean transparent) {
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet modelSet = mc.getModelManager().getBlockStateModelSet();
        BlockStateModel model = modelSet.get(facadeState);
        ModelBlockRenderer blockRenderer = ctx.getBlockRenderer();

        Block facadedBlock = level.getBlockState(pos).getBlock();
        float scale = computeScale(facadedBlock);

        float baseX = SectionPos.sectionRelative(pos.getX());
        float baseY = SectionPos.sectionRelative(pos.getY());
        float baseZ = SectionPos.sectionRelative(pos.getZ());

        IrisAwareBufferLookup bufferLookup = new IrisAwareBufferLookup(ctx, facadeState, pos, transparent);
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
                                         BlockAndTintGetter level, BlockPos pos, Direction face, BlockState facadeState,
                                         boolean transparent) {
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet modelSet = mc.getModelManager().getBlockStateModelSet();
        BlockStateModel model = modelSet.get(facadeState);
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), false, mc.getBlockColors());

        Block facadedBlock = level.getBlockState(pos).getBlock();
        float scale = computeScale(facadedBlock);

        float baseX = SectionPos.sectionRelative(pos.getX());
        float baseY = SectionPos.sectionRelative(pos.getY());
        float baseZ = SectionPos.sectionRelative(pos.getZ());

        IrisAwareBufferLookup bufferLookup = new IrisAwareBufferLookup(ctx, facadeState, pos, transparent);
        try {
            BlockQuadOutput output = slicingOutput(bufferLookup, face, scale);
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
                                                 Direction face, float scale) {
        return (x, y, z, quad, instance) -> {
            BakedQuad slicedQuad = CoverQuadRenderer.sliceQuad(quad, face);
            VertexConsumer buffer = bufferLookup.apply(slicedQuad.materialInfo().layer());
            if (scale == 1.0F) {
                buffer.putBlockBakedQuad(x, y, z, slicedQuad, instance);
            } else {
                emitScaledQuad(buffer, x, y, z, slicedQuad, instance, scale);
            }
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

    private static final class IrisAwareBufferLookup implements Function<ChunkSectionLayer, VertexConsumer> {
        private final AddSectionGeometryEvent.SectionRenderingContext ctx;
        private final BlockState facadeState;
        private final BlockPos pos;
        private final Set<VertexConsumer> touched;
        private final Map<ChunkSectionLayer, VertexConsumer> buffers;
        private final boolean irisActive;
        private final boolean transparent;

        IrisAwareBufferLookup(AddSectionGeometryEvent.SectionRenderingContext ctx, BlockState facadeState, BlockPos pos, boolean transparent) {
            this.ctx = ctx;
            this.facadeState = facadeState;
            this.pos = pos;
            this.irisActive = CFMain.isIrisLoaded();
            this.transparent = transparent;
            this.touched = this.irisActive ? new ReferenceOpenHashSet<>() : null;
            this.buffers = new EnumMap<>(ChunkSectionLayer.class);
        }

        @Override
        public VertexConsumer apply(ChunkSectionLayer layer) {
            ChunkSectionLayer effectiveLayer = transparent ? ChunkSectionLayer.TRANSLUCENT : layer;
            VertexConsumer cached = buffers.get(effectiveLayer);
            if (cached != null) {
                return cached;
            }

            VertexConsumer buffer = ctx.getOrCreateChunkBuffer(effectiveLayer);
            if (transparent) {
                buffer = IrisUtil.wrapAlpha(buffer);
            }
            if (irisActive && touched.add(buffer)) {
                IrisUtil.beginBlock(buffer, facadeState, pos);
            }
            buffers.put(effectiveLayer, buffer);
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
