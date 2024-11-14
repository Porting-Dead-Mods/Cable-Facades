package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemDecorator;

public class FacadeIconDecorator implements IItemDecorator {
    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int i, int i1) {
        if (itemStack.hasTag()) {
            CompoundTag nbt = itemStack.getTag();
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(nbt.getString(FacadeItem.FACADE_BLOCK)));
            PoseStack poseStack = guiGraphics.pose();

            poseStack.pushPose();
            {
                poseStack.translate(0, 0, -50);
                guiGraphics.renderItem(block.asItem().getDefaultInstance(), i, i1);
            }
            poseStack.popPose();
        }
        return true;
    }
}
