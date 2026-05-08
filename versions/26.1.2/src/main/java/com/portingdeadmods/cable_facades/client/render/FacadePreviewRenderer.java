package com.portingdeadmods.cable_facades.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class FacadePreviewRenderer {

    private static final long FACADE_RENDER_SEED = 42L;
    private static final int FULL_LIGHT = 0xF000F0;

    private FacadePreviewRenderer() {}

    public static void renderFullBlock(VertexConsumer buffer, PoseStack poseStack, Level level, BlockPos pos,
                                       BlockState facadeState, BlockStateModel model, float scale) {
        applyScale(poseStack, scale);
        List<BakedQuad> quads = collectQuads(model, facadeState, pos);
        emit(buffer, poseStack, level, pos, facadeState, quads);
    }

    public static void renderDirectional(VertexConsumer buffer, PoseStack poseStack, Level level, BlockPos pos,
                                         Direction face, BlockState facadeState, BlockStateModel model, float scale) {
        applyScale(poseStack, scale);
        List<BakedQuad> sliced = CoverQuadRenderer.sliceQuads(facadeState, pos, model, face);
        emit(buffer, poseStack, level, pos, facadeState, sliced);
    }

    private static void applyScale(PoseStack poseStack, float scale) {
        if (scale != 1.0F) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        }
    }

    private static List<BakedQuad> collectQuads(BlockStateModel model, BlockState state, BlockPos pos) {
        long seed = state.getSeed(pos);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(seed), parts);

        List<BakedQuad> result = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            result.addAll(part.getQuads(null));
            for (Direction dir : Direction.values()) {
                result.addAll(part.getQuads(dir));
            }
        }
        return result;
    }

    private static void emit(VertexConsumer buffer, PoseStack poseStack, Level level, BlockPos pos,
                             BlockState state, List<BakedQuad> quads) {
        if (quads.isEmpty()) return;
        BlockColors colors = Minecraft.getInstance().getBlockColors();
        BlockAndTintGetter tintGetter = level instanceof BlockAndTintGetter ? (BlockAndTintGetter) level : null;
        QuadInstance instance = new QuadInstance();
        instance.setLightCoords(FULL_LIGHT);
        instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

        for (BakedQuad quad : quads) {
            int tintIndex = quad.materialInfo().tintIndex();
            int tint = -1;
            if (tintIndex != -1) {
                BlockTintSource source = colors.getTintSource(state, tintIndex);
                if (source != null) {
                    tint = tintGetter != null
                            ? source.colorInWorld(state, tintGetter, pos)
                            : source.color(state);
                }
            }
            instance.setColor(tint);
            buffer.putBakedQuad(poseStack.last(), quad, instance);
        }
    }
}
