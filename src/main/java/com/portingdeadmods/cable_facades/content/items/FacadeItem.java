package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.events.client.ClientRegisterEvents;
import com.portingdeadmods.cable_facades.utils.FacadeItemNbt;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
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
                Block facadeBlock = FacadeItemNbt.getFacadeBlock(itemStack);

                ItemStack offhandItemStack = null;
                if (facadeBlock == null) {
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
                }

                if (!(facadeBlock.asItem() instanceof BlockItem)) {
                    return InteractionResult.FAIL;
                }

                FacadeType type = getFacadeType();
                if (!type.canApplyOn().test(level.getBlockState(pos))) {
                    return InteractionResult.FAIL;
                }

                Block targetBlock = level.getBlockState(pos).getBlock();

                if (targetBlock == facadeBlock || CFConfig.isBlockDisallowed(facadeBlock)) {
                    if (targetBlock == facadeBlock) {
                        context.getPlayer().displayClientMessage(Component.translatable("cable_facades.error.cannot_facade_itself").withStyle(ChatFormatting.RED), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("cable_facades.error.block_disabled").withStyle(ChatFormatting.RED), true);
                    }
                    return InteractionResult.FAIL;
                }

                FacadeUtils.addFacade(level, pos, facadeBlock.getStateForPlacement(new BlockPlaceContext(context)), type.id());

                if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                    itemStack.shrink(1);
                    if (offhandItemStack != null) {
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
        Block block = FacadeItemNbt.getFacadeBlock(itemStack);
        if (block != null && block.asItem() instanceof BlockItem blockItem) {
            return Component.translatable("cable_facades.facade.name_prefix").append(blockItem.getDescription());
        }
        return Component.translatable("cable_facades.facade.empty");
    }

    public ItemStack createFacade(Block block) {
        ItemStack facadeStack = new ItemStack(this);
        if (block != null && block.asItem() instanceof BlockItem) {
            FacadeItemNbt.setFacadeBlock(facadeStack, block);
        }
        return facadeStack;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        if (FacadeItemNbt.hasRemainder(itemStack)) {
            return this.getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ClientRegisterEvents.FACADE_ITEM_RENDERER;
            }
        });
    }
}
