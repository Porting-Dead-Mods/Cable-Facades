package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public FacadeItemRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(FacadeItem.FACADE_BLOCK)) {
                ResourceLocation blockId = new ResourceLocation(tag.getString(FacadeItem.FACADE_BLOCK));
                Block block = BuiltInRegistries.BLOCK.get(blockId);
                BlockState state = block.defaultBlockState();
                ItemStack defaultInstance = state.getBlock().asItem().getDefaultInstance();

                poseStack.pushPose();
                {
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.scale(2, 2, 2);
                    Minecraft.getInstance().getItemRenderer().renderStatic(Minecraft.getInstance().player, defaultInstance, ItemDisplayContext.FIXED, false, poseStack, buffer, Minecraft.getInstance().level, combinedLight, combinedOverlay, 0);
                }
                poseStack.popPose();
            }
        }

        float scaleFactor = 0.001f;

        poseStack.translate(-(scaleFactor / 2), -(scaleFactor / 2), -(scaleFactor / 2));
        poseStack.scale(1 + scaleFactor, 1 + scaleFactor, 1 + scaleFactor);
        var model = Minecraft.getInstance().getModelManager().getModel(
                new ResourceLocation(CFMain.MODID, "item/facade_outline"));
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
