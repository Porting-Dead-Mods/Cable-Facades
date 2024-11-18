package com.portingdeadmods.cable_facades.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.portingdeadmods.cable_facades.events.CFClientEvents;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class FacadeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public FacadeItemRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Optional<Block> optionalBlock = stack.get(CFDataComponents.FACADE_BLOCK);
        if (optionalBlock.isPresent()) {
            Block block = optionalBlock.get();
            BlockState state = block.defaultBlockState();

            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffer, combinedLight, combinedOverlay);

            var model = Minecraft.getInstance().getModelManager().getModel(CFClientEvents.FACADE_OUTLINE);
            Minecraft.getInstance().getItemRenderer().renderModelLists(
                    model,
                    stack,
                    combinedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer.getBuffer(model.getRenderTypes(stack, true).getFirst())
            );
        }
    }
}
