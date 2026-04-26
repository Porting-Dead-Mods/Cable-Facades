package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FacadeItemSpecialRenderer implements SpecialModelRenderer<Optional<Block>> {
    private static final float OUTLINE_SCALE_EPSILON = 0.001F;
    private static final float FLAT_THICKNESS_SCALE = 1.0F / 16.0F;
    private static final float FLAT_DEPTH_OFFSET = -7.5F / 16.0F;

    public FacadeItemSpecialRenderer() {
    }

    @Override
    public @Nullable Optional<Block> extractArgument(ItemStack itemStack) {
        return itemStack.get(CFDataComponents.FACADE_BLOCK);
    }

    @Override
    public void submit(@Nullable Optional<Block> block, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int i1, boolean b, int i2) {

    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {

    }

//    @Override
//    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
//        poseStack.pushPose();
//        {
//            Optional<Block> optionalBlock = stack.get(CFDataComponents.FACADE_BLOCK);
//            if (optionalBlock.isPresent() && optionalBlock.get().asItem() instanceof BlockItem blockItem) {
//                if (stack.is(CFItems.DIRECTIONAL_FACADE.get())) {
//                    renderDirectionalFacadeItem(optionalBlock.get().defaultBlockState(), stack, poseStack, buffer, combinedLight, combinedOverlay);
//                } else {
//                    ItemStack defaultInstance = blockItem.getDefaultInstance();
//
//                    poseStack.pushPose();
//                    {
//                        poseStack.translate(0.5, 0.5, 0.5);
//                        poseStack.scale(2, 2, 2);
//                        Minecraft.getInstance().getItemRenderer().renderStatic(Minecraft.getInstance().player, defaultInstance, ItemDisplayContext.FIXED, false, poseStack, buffer, Minecraft.getInstance().level, combinedLight, combinedOverlay, 0);
//                    }
//                    poseStack.popPose();
//                }
//            }
//
//            renderOutline(stack, poseStack, buffer, combinedLight);
//        }
//        poseStack.popPose();
//    }
//
//    private void renderDirectionalFacadeItem(BlockState state, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
//                                             int combinedLight, int combinedOverlay) {
//        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//        BakedModel model = blockRenderer.getBlockModel(state);
//        List<BakedQuad> quads = CoverQuadRenderer.sliceQuads(state, BlockPos.ZERO, model, Direction.SOUTH, ModelData.EMPTY);
//
//        poseStack.pushPose();
//        {
//            poseStack.translate(0.0F, 0.0F, FLAT_DEPTH_OFFSET);
//            for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(state.getSeed(BlockPos.ZERO)), ModelData.EMPTY)) {
//                VertexConsumer consumer = buffer.getBuffer(renderType);
//                for (BakedQuad quad : quads) {
//                    float red = 1.0F;
//                    float green = 1.0F;
//                    float blue = 1.0F;
//                    if (quad.getTintIndex() != -1) {
//                        int color = Minecraft.getInstance().getBlockColors().getColor(state, null, null, quad.getTintIndex());
//                        red = (color >> 16 & 0xFF) / 255.0F;
//                        green = (color >> 8 & 0xFF) / 255.0F;
//                        blue = (color & 0xFF) / 255.0F;
//                    }
//                    consumer.putBulkData(poseStack.last(), quad, red, green, blue, 1.0F, combinedLight, combinedOverlay);
//                }
//            }
//        }
//        poseStack.popPose();
//    }
//
//    private void renderOutline(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int combinedLight) {
//        var model = Minecraft.getInstance().getModelManager().getModel(ClientRegisterEvents.FACADE_OUTLINE);
//
//        poseStack.pushPose();
//        {
//            if (stack.is(CFItems.DIRECTIONAL_FACADE.get())) {
//                poseStack.translate(0.5F, 0.5F, 0.5F);
//                poseStack.scale(1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON, FLAT_THICKNESS_SCALE + OUTLINE_SCALE_EPSILON);
//                poseStack.translate(-0.5F, -0.5F, -0.5F);
//            } else {
//                poseStack.translate(-(OUTLINE_SCALE_EPSILON / 2.0F), -(OUTLINE_SCALE_EPSILON / 2.0F), -(OUTLINE_SCALE_EPSILON / 2.0F));
//                poseStack.scale(1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON);
//            }
//
//            Minecraft.getInstance().getItemRenderer().renderModelLists(
//                    model,
//                    stack,
//                    combinedLight,
//                    OverlayTexture.NO_OVERLAY,
//                    poseStack,
//                    buffer.getBuffer(model.getRenderTypes(stack, true).getFirst())
//            );
//        }
//        poseStack.popPose();
//    }

    public enum Unbaked implements SpecialModelRenderer.Unbaked<Optional<Block>> {
        INSTANCE;

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked.INSTANCE);

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public FacadeItemSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new FacadeItemSpecialRenderer();
        }
    }
}
