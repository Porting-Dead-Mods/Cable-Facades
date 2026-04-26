package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class FacadeItem extends Item {
    public FacadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();
        InteractionResult result = InteractionResult.FAIL;

        if (!level.isClientSide()) {
            if (!FacadeUtils.hasFacade(level, pos)) {
                Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);

                ItemStack offhandItemStack = null;
                Block block1;
                if(block.isEmpty()){
                    // If the player is holding an empty facade in their mainhand
                    // and a block in their offhand, try to use that block.
                    if (context.getHand() == InteractionHand.MAIN_HAND) {
                        ItemStack offhand = context.getPlayer().getItemInHand(InteractionHand.OFF_HAND);
                        Item item = offhand.getItem();
                        if (item instanceof BlockItem blockItem) {
                            offhandItemStack = offhand;
                            block1 = blockItem.getBlock();
                        } else {
                            return InteractionResult.FAIL;
                        }
                    } else {
                        return InteractionResult.FAIL;
                    }
                } else {
                    block1 = block.get();
                }

                // Validate that the facade block is valid (not air or non-BlockItem)
                if (!(block1.asItem() instanceof BlockItem)) {
                    return InteractionResult.FAIL;
                }

                Block targetBlock = context.getLevel().getBlockState(pos).getBlock();

                boolean noFacadeTag = context.getLevel().getBlockState(pos).tags().noneMatch(blockTagKey -> blockTagKey.equals(CFItemTags.SUPPORTS_FACADE));

                // Check that the block is part of the config or has the tag
                if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) {
                    return InteractionResult.FAIL;
                }

                // Prevent block from being facaded with itself or if it's disallowed
                if (targetBlock == block1 || CFConfig.isBlockDisallowed(block1)) {
                    if (targetBlock == block1) {
                        context.getPlayer().sendOverlayMessage(Component.translatable("cable_facades.error.cannot_facade_itself").withStyle(ChatFormatting.RED));
                    } else {
                        context.getPlayer().sendOverlayMessage(Component.translatable("cable_facades.error.block_disabled").withStyle(ChatFormatting.RED));
                    }
                    return InteractionResult.FAIL;
                }

                FacadeUtils.addFacade(level, pos, block1.getStateForPlacement(new BlockPlaceContext(context)));

                if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                    itemStack.shrink(1);
                    if(offhandItemStack != null){
                        offhandItemStack.shrink(1);
                    }
                }
            }
            result = InteractionResult.SUCCESS_SERVER;
        }

        FacadeUtils.updateBlocks(level, pos);


        return result;
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);
        if (block.isPresent() && block.get().asItem() instanceof BlockItem blockItem) {
            return Component.translatable("cable_facades.facade.name_prefix");//.append(blockItem.getDescription());
        }
        return Component.translatable("cable_facades.facade.empty");
    }

    public ItemStack createFacade(Block block) {
        ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
        // Only set the facade block if it's a valid BlockItem (not air or other non-block items)
        if (block != null && block.asItem() instanceof BlockItem) {
            facadeStack.set(CFDataComponents.FACADE_BLOCK, Optional.of(block));
        }
        return facadeStack;
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        if (Boolean.TRUE.equals(instance.get(CFDataComponents.HAS_FACADE_REMAINDER))) {
            return new ItemStackTemplate(this);
        }

        return super.getCraftingRemainder(instance);
    }
}
