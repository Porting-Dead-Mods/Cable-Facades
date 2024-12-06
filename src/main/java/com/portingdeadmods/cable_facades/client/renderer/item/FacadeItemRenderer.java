package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public FacadeItemRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(FacadeItem.FACADE_BLOCK)) {
                ResourceLocation blockId = new ResourceLocation(tag.getString(FacadeItem.FACADE_BLOCK));
                Block block = Registry.BLOCK.get(blockId);
                BlockState state = block.defaultBlockState();

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffer, combinedLight, combinedOverlay);
            }
        }

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
    }
}
