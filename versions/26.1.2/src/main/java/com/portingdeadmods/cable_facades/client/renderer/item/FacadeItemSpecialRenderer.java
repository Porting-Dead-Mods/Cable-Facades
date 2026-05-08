package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.client.render.CoverQuadRenderer;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FacadeItemSpecialRenderer implements SpecialModelRenderer<Block> {

    private static final float FLAT_DEPTH_OFFSET = -7.5F / 16.0F;
    private static final float FLAT_THICKNESS_SCALE = 1.0F / 16.0F;
    private static final Direction OUTLINE_UNIFORM_NORMAL = Direction.SOUTH;

    private final Mode mode;
    private final List<BakedQuad> outlineQuads;

    public FacadeItemSpecialRenderer(Mode mode, List<BakedQuad> outlineQuads) {
        this.mode = mode;
        this.outlineQuads = outlineQuads;
    }

    @Override
    public @Nullable Block extractArgument(ItemStack stack) {
        return stack.get(CFDataComponents.FACADE_BLOCK)
                .filter(block -> block.asItem() instanceof BlockItem)
                .orElse(null);
    }

    @Override
    public void submit(@Nullable Block block, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (block == null) {
            renderOutlineOnly(poseStack, collector, lightCoords, overlayCoords, outlineColor);
            return;
        }

        BlockState state = block.defaultBlockState();
        Minecraft mc = Minecraft.getInstance();
        BlockStateModelSet modelSet = mc.getModelManager().getBlockStateModelSet();
        BlockStateModel model = modelSet.get(state);

        switch (mode) {
            case FULL_BLOCK -> renderFullBlock(state, model, poseStack, collector, lightCoords, overlayCoords, outlineColor);
            case DIRECTIONAL -> renderDirectional(state, model, poseStack, collector, lightCoords, overlayCoords, outlineColor);
        }
    }

    private void renderFullBlock(BlockState state, BlockStateModel model, PoseStack poseStack,
                                 SubmitNodeCollector collector, int light, int overlay, int outlineColor) {
        long seed = state.getSeed(BlockPos.ZERO);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(seed), parts);

        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts) {
            quads.addAll(part.getQuads(null));
            for (Direction dir : Direction.values()) {
                quads.addAll(part.getQuads(dir));
            }
        }
        quads.addAll(outlineQuads);

        int[] tints = computeTintLayers(state, quads);
        ItemDisplayContext context = ItemDisplayContext.FIXED;
        collector.submitItem(poseStack, context, light, overlay, outlineColor, tints, quads, ItemStackRenderState.FoilType.NONE);
    }

    private void renderDirectional(BlockState state, BlockStateModel model, PoseStack poseStack,
                                   SubmitNodeCollector collector, int light, int overlay, int outlineColor) {
        List<BakedQuad> sliced = CoverQuadRenderer.sliceQuads(state, BlockPos.ZERO, model, Direction.SOUTH);
        int[] tints = computeTintLayers(state, sliced);
        ItemDisplayContext context = ItemDisplayContext.FIXED;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, FLAT_DEPTH_OFFSET);
        collector.submitItem(poseStack, context, light, overlay, outlineColor, tints, sliced, ItemStackRenderState.FoilType.NONE);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(1.0F, 1.0F, FLAT_THICKNESS_SCALE);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        collector.submitItem(poseStack, context, light, overlay, outlineColor, new int[0], outlineQuads, ItemStackRenderState.FoilType.NONE);
        poseStack.popPose();
    }

    private void renderOutlineOnly(PoseStack poseStack, SubmitNodeCollector collector,
                                   int light, int overlay, int outlineColor) {
        ItemDisplayContext context = ItemDisplayContext.FIXED;
        if (mode == Mode.DIRECTIONAL) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(1.0F, 1.0F, FLAT_THICKNESS_SCALE);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            collector.submitItem(poseStack, context, light, overlay, outlineColor, new int[0], outlineQuads, ItemStackRenderState.FoilType.NONE);
            poseStack.popPose();
        } else {
            collector.submitItem(poseStack, context, light, overlay, outlineColor, new int[0], outlineQuads, ItemStackRenderState.FoilType.NONE);
        }
    }

    private static int[] computeTintLayers(BlockState state, List<BakedQuad> quads) {
        int maxTintIndex = -1;
        for (BakedQuad quad : quads) {
            int tintIndex = quad.materialInfo().tintIndex();
            if (tintIndex > maxTintIndex) {
                maxTintIndex = tintIndex;
            }
        }
        if (maxTintIndex < 0) {
            return new int[0];
        }

        BlockColors colors = Minecraft.getInstance().getBlockColors();
        int[] result = new int[maxTintIndex + 1];
        for (int i = 0; i <= maxTintIndex; i++) {
            BlockTintSource source = colors.getTintSource(state, i);
            result[i] = source != null ? source.color(state) : -1;
        }
        return result;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
    }

    public enum Mode {
        FULL_BLOCK,
        DIRECTIONAL
    }

    public static BakedQuad retargetNormal(BakedQuad source) {
        return new BakedQuad(
                source.position0(), source.position1(), source.position2(), source.position3(),
                source.packedUV0(), source.packedUV1(), source.packedUV2(), source.packedUV3(),
                OUTLINE_UNIFORM_NORMAL,
                source.materialInfo()
        );
    }

    public record Unbaked(Mode mode) implements SpecialModelRenderer.Unbaked<Block> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("mode")
                        .xmap(Mode::valueOf, Mode::name)
                        .forGetter(Unbaked::mode)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<Block> bake(SpecialModelRenderer.BakingContext context) {
            return new FacadeItemSpecialRenderer(mode, List.of());
        }
    }
}
