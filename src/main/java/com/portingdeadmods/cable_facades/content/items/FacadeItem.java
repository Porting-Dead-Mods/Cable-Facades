package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

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

        if (!level.isClientSide()) {
            if (!FacadeUtils.hasFacade(level, pos)) {
                Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);
                if (block.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                Block block1 = block.get();

                Block targetBlock = context.getLevel().getBlockState(pos).getBlock();

                boolean noFacadeTag = context.getLevel().getBlockState(pos).getTags().noneMatch(blockTagKey -> blockTagKey.equals(CFItemTags.SUPPORTS_FACADE));

                // Check that the block is part of the config or has the tag
                if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) {
                    return InteractionResult.FAIL;
                }

                // Prevent block from being facaded with itself or if it's disallowed
                if (targetBlock == block1 || CFConfig.isBlockDisallowed(block1)) {
                    if(targetBlock == block1){
                        context.getPlayer().displayClientMessage(Component.literal("Cannot facade block with itself").withStyle(ChatFormatting.RED),true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.literal("This block cannot be used as a cover (disabled by config)").withStyle(ChatFormatting.RED),true);
                    }
                    return InteractionResult.FAIL;
                }

                FacadeUtils.addFacade(level, pos, block1.getStateForPlacement(new BlockPlaceContext(context)));

                if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                    itemStack.shrink(1);
                }
            }
        }

        FacadeUtils.updateBlocks(level, pos);


        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);
        if (block.isPresent()) {
            BlockItem blockItem = (BlockItem) block.get().asItem();
            return Component.literal("Facade - " + blockItem.getDescription().getString());
        }
        return Component.literal("Facade - Empty");
    }

    public ItemStack createFacade(Block block) {
        ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
        facadeStack.set(CFDataComponents.FACADE_BLOCK, Optional.of(block));
        return facadeStack;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        if (Boolean.TRUE.equals(itemStack.get(CFDataComponents.HAS_FACADE_REMAINDER))) {
            return this.getDefaultInstance();
        }

        return ItemStack.EMPTY;
    }
}
