package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import java.util.function.Supplier;

public class FacadeItem extends Item {

    private final Supplier<FacadeType> facadeType;

    public FacadeItem(Properties properties) {
        this(properties, FacadeTypes::defaultType);
    }

    public FacadeItem(Properties properties, Supplier<FacadeType> facadeType) {
        super(properties);
        this.facadeType = facadeType;
    }

    public FacadeType getFacadeType() {
        FacadeType type = facadeType.get();
        return type != null ? type : FacadeTypes.defaultType();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack itemStack = context.getItemInHand();

        if (!level.isClientSide()) {
            if (!FacadeUtils.hasFacade(level, pos)) {
                Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);

                ItemStack offhandItemStack = null;
                Block block1;
                if(block.isEmpty()){
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

                if (!(block1.asItem() instanceof BlockItem)) {
                    return InteractionResult.FAIL;
                }

                FacadeType type = getFacadeType();
                if (!type.canApplyOn().test(level.getBlockState(pos))) {
                    return InteractionResult.FAIL;
                }

                Block targetBlock = level.getBlockState(pos).getBlock();

                if (targetBlock == block1 || CFConfig.isBlockDisallowed(block1)) {
                    if (targetBlock == block1) {
                        context.getPlayer().displayClientMessage(Component.translatable("cable_facades.error.cannot_facade_itself").withStyle(ChatFormatting.RED), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("cable_facades.error.block_disabled").withStyle(ChatFormatting.RED), true);
                    }
                    return InteractionResult.FAIL;
                }

                FacadeUtils.addFacade(level, pos, block1.getStateForPlacement(new BlockPlaceContext(context)), type.id());

                if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                    itemStack.shrink(1);
                    if(offhandItemStack != null){
                        offhandItemStack.shrink(1);
                    }
                }
            }
        }

        FacadeUtils.updateBlocks(level, pos);


        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        Optional<Block> block = itemStack.get(CFDataComponents.FACADE_BLOCK);
        if (block.isPresent() && block.get().asItem() instanceof BlockItem blockItem) {
            return Component.translatable("cable_facades.facade.name_prefix").append(blockItem.getDescription());
        }
        return Component.translatable("cable_facades.facade.empty");
    }

    public ItemStack createFacade(Block block) {
        ItemStack facadeStack = new ItemStack(this);
        if (block != null && block.asItem() instanceof BlockItem) {
            facadeStack.set(CFDataComponents.FACADE_BLOCK, Optional.of(block));
        }
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
