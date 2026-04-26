package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;

import java.util.List;

public final class FacadeRenderer {
    public static void renderFullBlockFacade(BlockQuadOutput output, PoseStack poseStack, ModelBlockRenderer blockRenderer,
                                             BlockAndTintGetter level, RandomSource random,
                                             BlockPos pos, BlockState facadeState) {
        random.setSeed(FacadeRenderConstants.FACADE_RENDER_SEED);

        poseStack.pushPose();
        //poseStack.translate(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));

        Block facadedBlock = level.getBlockState(pos).getBlock();

        if (CFConfig.canPatchZFighting(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(FacadeRenderConstants.ZFIGHTING_SCALE, FacadeRenderConstants.ZFIGHTING_SCALE, FacadeRenderConstants.ZFIGHTING_SCALE);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        if (CFConfig.isScaleUpBlock(facadedBlock)) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(FacadeRenderConstants.SCALE_UP_FACTOR, FacadeRenderConstants.SCALE_UP_FACTOR, FacadeRenderConstants.SCALE_UP_FACTOR);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        Minecraft mc = Minecraft.getInstance();
        blockRenderer.tesselateBlock(
                output,
                SectionPos.sectionRelative(pos.getX()),
                SectionPos.sectionRelative(pos.getY()),
                SectionPos.sectionRelative(pos.getZ()),
                level,
                pos,
                facadeState,
                mc.getModelManager().getBlockStateModelSet().get(facadeState),
                facadeState.getSeed(pos)
        );

        poseStack.popPose();
    }

    public static void renderDirectionalFacade(BlockQuadOutput output, PoseStack poseStack, ModelBlockRenderer blockRenderer,
                                                BlockAndTintGetter level, RandomSource random,
                                                BlockPos pos, Direction face, BlockState facadeState) {
        random.setSeed(FacadeRenderConstants.FACADE_RENDER_SEED);
        BlockStateModelSet blockStateModelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

        BlockStateModel facadeModel = blockStateModelSet.get(facadeState);
        boolean useAo = net.neoforged.neoforge.client.config.NeoForgeClientConfig.INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean() && Minecraft.getInstance().options.ambientOcclusion().get();

        List<BakedQuad> slicedQuads = CoverQuadRenderer.sliceQuads(level, facadeState, pos, facadeModel, face);

        poseStack.pushPose();
        poseStack.translate(SectionPos.sectionRelative(pos.getX()), SectionPos.sectionRelative(pos.getY()), SectionPos.sectionRelative(pos.getZ()));
        for (BakedQuad quad : slicedQuads) {
            float x = SectionPos.sectionRelative(pos.getX());
            float y = SectionPos.sectionRelative(pos.getY());
            float z = SectionPos.sectionRelative(pos.getZ());

            Vec3 offset = facadeState.getOffset(pos);

//            if (CFMain.isIrisLoaded()) {
//                IrisUtil.beginBlock(buffer, facadeState, pos);
//            }
            FacadeQuadLighter.renderQuad(level, blockRenderer, facadeState, pos, poseStack.last(), output, quad, (float) (x + offset.x), (float) (y + offset.y), (float) (z + offset.z),
                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, useAo);
//            if (CFMain.isIrisLoaded()) {
//                IrisUtil.endBlock(buffer);
//            }
        }

        poseStack.popPose();
    }

}
