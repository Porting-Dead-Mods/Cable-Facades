package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.*;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.compat.iris.AlphaWrapperIris;
import com.portingdeadmods.cable_facades.compat.iris.IrisUtil;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;
import java.util.Set;

public final class ChunkFacadeBaker {

    private static final long FACADE_RENDER_SEED = 42L;
    private static final float ZFIGHTING_SCALE = 0.99995F;
    private static final float SCALE_UP_FACTOR = 1.0005F;
    private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::create);

    private ChunkFacadeBaker() {}

    private static void beginLayer(BufferBuilder bufferBuilder) {
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }

    public static void bakeSection(BlockPos sectionOrigin, RenderChunkRegion region,
                                   ChunkBufferBuilderPack pack, Set<RenderType> usedTypes) {
        if (ClientFacadeManager.isEmpty()) return;

        SectionPos section = SectionPos.of(sectionOrigin);

        GameClientEvents.RENDERING_FACADE.set(true);
        try {
            RandomSource random = RANDOM.get();

            ClientFacadeManager.entryStream()
                    .filter(e -> SectionPos.of(e.getKey()).equals(section))
                    .forEach(entry -> {
                        BlockPos pos = entry.getKey();
                        FacadeData data = entry.getValue();
                        if (data == null) return;
                        if (data.isFullBlock()) {
                            renderFullBlockFacade(region, pack, usedTypes, random, pos, data.getFullBlock());
                        } else if (data.isDirectional()) {
                            data.directional().forEach((dir, state) ->
                                    renderDirectionalFacade(region, pack, usedTypes, random, pos, dir, state));
                        }
                    });
        } finally {
            GameClientEvents.RENDERING_FACADE.set(false);
        }
    }

    private static void renderFullBlockFacade(RenderChunkRegion region, ChunkBufferBuilderPack pack,
                                              Set<RenderType> usedTypes,
                                              RandomSource random, BlockPos pos, BlockState facadeState) {
        random.setSeed(FACADE_RENDER_SEED);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
        ModelData modelData = facadeModel.getModelData(region, pos, facadeState, ModelData.EMPTY);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));

        Block facadedBlock = region.getBlockState(pos).getBlock();

        if (CFConfig.canPatchZFighting(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(ZFIGHTING_SCALE, ZFIGHTING_SCALE, ZFIGHTING_SCALE);
            poseStack.translate(-0.5, -0.5, -0.5);
        }
        if (CFConfig.isScaleUpBlock(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(SCALE_UP_FACTOR, SCALE_UP_FACTOR, SCALE_UP_FACTOR);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
            RenderType targetType = GameClientEvents.facadeTransparency ? RenderType.translucent() : renderType;
            BufferBuilder bufferBuilder = pack.builder(targetType);
            if (usedTypes.add(targetType)) {
                beginLayer(bufferBuilder);
            }
            VertexConsumer buffer = bufferBuilder;
            if (GameClientEvents.facadeTransparency) {
                buffer = CFMain.isOculusLoaded() ? new AlphaWrapperIris(buffer) : new GameClientEvents.AlphaWrapper(buffer);
            }
            if (CFMain.isOculusLoaded()) {
                IrisUtil.beginBlock(buffer, facadeState, pos);
            }
            blockRenderer.renderBatched(facadeState, pos, region, poseStack, buffer, true, random, modelData, renderType);
            if (CFMain.isOculusLoaded()) {
                IrisUtil.endBlock(buffer);
            }
        }
    }

    private static void renderDirectionalFacade(RenderChunkRegion region, ChunkBufferBuilderPack pack,
                                                Set<RenderType> usedTypes,
                                                RandomSource random, BlockPos pos, Direction face, BlockState facadeState) {
        random.setSeed(FACADE_RENDER_SEED);
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel facadeModel = blockRenderer.getBlockModel(facadeState);
        ModelData modelData = facadeModel.getModelData(region, pos, facadeState, ModelData.EMPTY);
        boolean useAo = Minecraft.useAmbientOcclusion()
                && facadeState.getLightEmission() == 0
                && facadeModel.useAmbientOcclusion();

        List<BakedQuad> slicedQuads = CoverQuadRenderer.sliceQuads(facadeState, pos, facadeModel, face, modelData);

        PoseStack poseStack = new PoseStack();
        poseStack.translate(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));

        for (RenderType renderType : facadeModel.getRenderTypes(facadeState, random, ModelData.EMPTY)) {
            RenderType targetType = GameClientEvents.facadeTransparency ? RenderType.translucent() : renderType;
            BufferBuilder bufferBuilder = pack.builder(targetType);
            if (usedTypes.add(targetType)) {
                beginLayer(bufferBuilder);
            }
            VertexConsumer buffer = bufferBuilder;
            if (GameClientEvents.facadeTransparency) {
                buffer = CFMain.isOculusLoaded() ? new AlphaWrapperIris(buffer) : new GameClientEvents.AlphaWrapper(buffer);
            }
            if (CFMain.isOculusLoaded()) {
                IrisUtil.beginBlock(buffer, facadeState, pos);
            }
            for (BakedQuad quad : slicedQuads) {
                float r = 1f, g = 1f, b = 1f;
                if (quad.getTintIndex() != -1) {
                    int color = Minecraft.getInstance().getBlockColors().getColor(facadeState, region, pos, quad.getTintIndex());
                    r = (color >> 16 & 0xFF) / 255f;
                    g = (color >> 8 & 0xFF) / 255f;
                    b = (color & 0xFF) / 255f;
                }
                FacadeQuadLighter.renderQuad(region, facadeState, pos, poseStack.last(), buffer, quad, r, g, b,
                        OverlayTexture.NO_OVERLAY, useAo);
            }
            if (CFMain.isOculusLoaded()) {
                IrisUtil.endBlock(buffer);
            }
        }
    }
}
