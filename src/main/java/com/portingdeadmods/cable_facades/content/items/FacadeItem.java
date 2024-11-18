package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FacadeItem extends Item {
    public static final String FACADE_BLOCK = "facade_block";

    public FacadeItem(Properties properties) {
        super(properties);
    }

    // DEBUGGING CODE
    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        if (p_41432_.isClientSide()) {
            CFMain.LOGGER.debug("Facades: {}", ClientFacadeManager.FACADED_BLOCKS);
            CFMain.LOGGER.debug("Loaded: {}", ClientFacadeManager.LOADED_BLOCKS);
        }
        return super.use(p_41432_, p_41433_, p_41434_);
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

                // Prevent block from being facaded with itself
                if (targetBlock == block1) {
                    return InteractionResult.FAIL;
                }

                FacadeUtils.addFacade(level, pos, block1);

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
}
