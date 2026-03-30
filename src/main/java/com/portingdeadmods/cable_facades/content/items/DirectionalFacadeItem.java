package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
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

public class DirectionalFacadeItem extends Item {
    public DirectionalFacadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        ItemStack itemStack = context.getItemInHand();

        if (!level.isClientSide()) {
            if (FacadeUtils.getFacade(level, pos) != null) {
                return InteractionResult.FAIL;
            }

            if (FacadeUtils.getDirectionalFacade(level, pos, clickedFace) != null) {
                return InteractionResult.FAIL;
            }

            Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);

            ItemStack offhandItemStack = null;
            Block facadeBlock;
            if (block == null || block.isEmpty()) {
                if (context.getHand() == InteractionHand.MAIN_HAND) {
                    ItemStack offhand = context.getPlayer().getItemInHand(InteractionHand.OFF_HAND);
                    if (offhand.getItem() instanceof BlockItem blockItem) {
                        offhandItemStack = offhand;
                        facadeBlock = blockItem.getBlock();
                    } else {
                        return InteractionResult.FAIL;
                    }
                } else {
                    return InteractionResult.FAIL;
                }
            } else {
                facadeBlock = block.get();
            }

            if (!(facadeBlock.asItem() instanceof BlockItem)) {
                return InteractionResult.FAIL;
            }

            Block targetBlock = level.getBlockState(pos).getBlock();
            boolean noFacadeTag = level.getBlockState(pos).getTags().noneMatch(blockTagKey -> blockTagKey.equals(CFItemTags.SUPPORTS_FACADE));

            if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) {
                return InteractionResult.FAIL;
            }

            if (targetBlock == facadeBlock || CFConfig.isBlockDisallowed(facadeBlock)) {
                if (targetBlock == facadeBlock) {
                    context.getPlayer().displayClientMessage(Component.translatable("cable_facades.error.cannot_facade_itself").withStyle(ChatFormatting.RED), true);
                } else {
                    context.getPlayer().displayClientMessage(Component.translatable("cable_facades.error.block_disabled").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.FAIL;
            }

            FacadeUtils.addDirectionalFacade(level, pos, clickedFace, facadeBlock.getStateForPlacement(new BlockPlaceContext(context)));

            if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                itemStack.shrink(1);
                if (offhandItemStack != null) {
                    offhandItemStack.shrink(1);
                }
            }
        }

        FacadeUtils.updateBlocks(level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);
        if (block != null && block.isPresent() && block.get().asItem() instanceof BlockItem blockItem) {
            return Component.translatable("cable_facades.directional_facade.name_prefix").append(blockItem.getDescription());
        }
        return Component.translatable("cable_facades.directional_facade.empty");
    }

    public ItemStack createFacade(Block block) {
        ItemStack stack = new ItemStack(CFItems.DIRECTIONAL_FACADE.get());
        if (block != null && block.asItem() instanceof BlockItem) {
            stack.set(CFDataComponents.FACADE_BLOCK, Optional.of(block));
        }
        return stack;
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
