package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.client.render.CoverQuadRenderer;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.events.client.ClientRegisterEvents;
import com.portingdeadmods.cable_facades.utils.FacadeItemNbt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public class FacadeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float OUTLINE_SCALE_EPSILON = 0.001F;
    private static final float FLAT_THICKNESS_SCALE = 1.0F / 16.0F;
    private static final float FLAT_DEPTH_OFFSET = -7.5F / 16.0F;

    public FacadeItemRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();
        Item itemType = stack.getItem();
        boolean isDirectional = itemType instanceof DirectionalFacadeItem;
        Block block = FacadeItemNbt.getFacadeBlock(stack);
        if (block != null && block.asItem() instanceof BlockItem blockItem) {
            if (isDirectional) {
                renderDirectionalFacadeItem(block.defaultBlockState(), poseStack, buffer, combinedLight, combinedOverlay);
            } else {
                ItemStack defaultInstance = blockItem.getDefaultInstance();
                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(2, 2, 2);
                Minecraft.getInstance().getItemRenderer().renderStatic(Minecraft.getInstance().player, defaultInstance, ItemDisplayContext.FIXED, false, poseStack, buffer, Minecraft.getInstance().level, combinedLight, combinedOverlay, 0);
                poseStack.popPose();
            }
        }

        renderOutline(stack, poseStack, buffer, combinedLight, isDirectional);
        poseStack.popPose();
    }

    private void renderDirectionalFacadeItem(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
                                             int combinedLight, int combinedOverlay) {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = blockRenderer.getBlockModel(state);
        List<BakedQuad> quads = CoverQuadRenderer.sliceQuads(state, BlockPos.ZERO, model, Direction.SOUTH, ModelData.EMPTY);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, FLAT_DEPTH_OFFSET);
        for (RenderType renderType : model.getRenderTypes(state, RandomSource.create(state.getSeed(BlockPos.ZERO)), ModelData.EMPTY)) {
            VertexConsumer consumer = buffer.getBuffer(renderType);
            for (BakedQuad quad : quads) {
                float red = 1.0F, green = 1.0F, blue = 1.0F;
                if (quad.getTintIndex() != -1) {
                    int color = Minecraft.getInstance().getBlockColors().getColor(state, null, null, quad.getTintIndex());
                    red = (color >> 16 & 0xFF) / 255.0F;
                    green = (color >> 8 & 0xFF) / 255.0F;
                    blue = (color & 0xFF) / 255.0F;
                }
                consumer.putBulkData(poseStack.last(), quad, red, green, blue, combinedLight, combinedOverlay);
            }
        }
        poseStack.popPose();
    }

    private void renderOutline(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, boolean isDirectional) {
        ModelResourceLocation outlineId = resolveOutlineModel(stack);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(outlineId);

        poseStack.pushPose();
        if (isDirectional) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON, FLAT_THICKNESS_SCALE + OUTLINE_SCALE_EPSILON);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        } else {
            poseStack.translate(-(OUTLINE_SCALE_EPSILON / 2.0F), -(OUTLINE_SCALE_EPSILON / 2.0F), -(OUTLINE_SCALE_EPSILON / 2.0F));
            poseStack.scale(1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON, 1.0F + OUTLINE_SCALE_EPSILON);
        }

        List<RenderType> renderTypes = model.getRenderTypes(stack, true);
        RenderType renderType = renderTypes.isEmpty() ? RenderType.solid() : renderTypes.get(0);
        Minecraft.getInstance().getItemRenderer().renderModelLists(
                model, stack, combinedLight, OverlayTexture.NO_OVERLAY, poseStack,
                buffer.getBuffer(renderType)
        );
        poseStack.popPose();
    }

    private static ModelResourceLocation resolveOutlineModel(ItemStack stack) {
        Item item = stack.getItem();
        FacadeType type = null;
        if (item instanceof FacadeItem facade) {
            type = facade.getFacadeType();
        } else if (item instanceof DirectionalFacadeItem directional) {
            type = directional.getFacadeType();
        }
        if (type == null) {
            type = FacadeTypes.defaultType();
        }
        ResourceLocation outline = type != null ? type.outlineModel() : null;
        if (outline == null) {
            return ClientRegisterEvents.FACADE_OUTLINE;
        }
        return new ModelResourceLocation(outline, "inventory");
    }
}
