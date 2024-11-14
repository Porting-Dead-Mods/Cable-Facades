package com.portingdeadmods.cable_facades.rendeer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final BlockRenderDispatcher blockRenderer;

    public FacadeItemRenderer(BlockRenderDispatcher blockRenderer, EntityModelSet entityModelSet) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), entityModelSet);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        System.out.println("Test Rendering facade item");
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(FacadeItem.FACADE_BLOCK)) {
                ResourceLocation blockId = new ResourceLocation(tag.getString(FacadeItem.FACADE_BLOCK));
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                BlockState state = block.defaultBlockState();
                System.out.println("Rendering facade for block: " + blockId);

                poseStack.pushPose();
                switch (displayContext) {
                    case GUI -> {
                        poseStack.translate(0, 0, 0);
                        poseStack.scale(1.0F, -1.0F, 1.0F);
                    }
                    case GROUND -> {
                        poseStack.translate(0.5F, 0.25F, 0.5F);
                        poseStack.scale(0.5F, 0.5F, 0.5F);
                    }
                    case FIXED -> {
                        poseStack.translate(0.5F, 0.5F, 0.5F);
                        poseStack.scale(1.0F, -1.0F, -1.0F);
                    }
                    default -> {
                        poseStack.translate(0.5F, 0.5F, 0.5F);
                    }
                }

                blockRenderer.renderSingleBlock(state, poseStack, buffer, combinedLight, combinedOverlay);
                poseStack.popPose();
            }
        }

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0, 0, -0.01F);
        }

        var model = Minecraft.getInstance().getModelManager().getModel(
                new ResourceLocation(CFMain.MODID, "item/facade"));

        Minecraft.getInstance().getItemRenderer().renderModelLists(
                model,
                stack,
                combinedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer.getBuffer(model.getRenderTypes(stack, true).get(0))
        );
        poseStack.popPose();
    }
}
