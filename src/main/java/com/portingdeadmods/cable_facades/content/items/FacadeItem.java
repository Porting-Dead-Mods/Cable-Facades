package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientStuff;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

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
            if (itemStack.hasTag() && !FacadeUtils.hasFacade(level, pos)) {
                CompoundTag tag = itemStack.getTag();
                Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString(FACADE_BLOCK)));
                Block targetBlock = context.getLevel().getBlockState(pos).getBlock();

                boolean noFacadeTag = context.getLevel().getBlockState(pos).getTags().noneMatch(blockTagKey -> blockTagKey.equals(CFItemTags.SUPPORTS_FACADE));

                // Check that the block is part of the config or has the tag
                if (!CFConfig.isBlockAllowed(targetBlock) && noFacadeTag) {
                    return InteractionResult.FAIL;
                }

                // Prevent block from being facaded with itself or if it's disallowed
                if (targetBlock == block || CFConfig.isBlockDisallowed(block)) {
                   if(targetBlock == block){
                       context.getPlayer().displayClientMessage(Component.literal("Cannot facade block with itself").withStyle(ChatFormatting.RED),true);
                   } else {
                       context.getPlayer().displayClientMessage(Component.literal("This block cannot be used as a cover (disabled by config)").withStyle(ChatFormatting.RED),true);
                   }
                    return InteractionResult.FAIL;
                }

                FacadeUtils.addFacade(level, pos, block.getStateForPlacement(new BlockPlaceContext(context)));

                if (!context.getPlayer().isCreative() && CFConfig.consumeFacade) {
                    itemStack.shrink(1);
                }
            }
        }

        FacadeUtils.updateBlocks(level, pos);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString(FACADE_BLOCK)));
            BlockItem blockItem = (BlockItem) block.asItem();
            return Component.literal("Facade - " + blockItem.getDescription().getString());
        }
        return Component.literal("Facade - Empty");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ClientStuff.FACADE_ITEM_RENDERER;
            }
        });
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && Boolean.TRUE.equals(tag.getBoolean("has_facade_remainder"))) {
            return this.getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack createFacade(Block block) {
        ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
        CompoundTag nbtData = new CompoundTag();
        nbtData.putString(FacadeItem.FACADE_BLOCK, BuiltInRegistries.BLOCK.getKey(block).toString());
        facadeStack.setTag(nbtData);
        return facadeStack;
    }


}
