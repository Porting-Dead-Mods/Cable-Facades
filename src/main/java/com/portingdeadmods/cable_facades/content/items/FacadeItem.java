package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.CFClientEvents;
import com.portingdeadmods.cable_facades.rendeer.ClientStuff;
import com.portingdeadmods.cable_facades.rendeer.FacadeItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class FacadeItem extends Item {
    public static final String FACADE_BLOCK = "facade_block";

    public FacadeItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        ItemStack itemStack = p_41427_.getItemInHand();
        if (itemStack.hasTag()) {
            CompoundTag tag = itemStack.getTag();
            BlockPos pos = p_41427_.getClickedPos();
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString(FACADE_BLOCK)));
            Block targetBlock = p_41427_.getLevel().getBlockState(pos).getBlock();

            if (!CFConfig.isBlockAllowed(targetBlock)) {
                return InteractionResult.FAIL;
            }

            if (targetBlock == block) {
                return InteractionResult.FAIL;
            }

            boolean isAlreadyCamouflaged = p_41427_.getLevel() instanceof ServerLevel serverLevel
                    ? CableFacadeSavedData.get(serverLevel).contains(pos)
                    : CFClientEvents.CAMOUFLAGED_BLOCKS.containsKey(pos);

            if (isAlreadyCamouflaged) {
                return InteractionResult.FAIL;
            }

            if (p_41427_.getLevel() instanceof ServerLevel serverLevel) {
                CableFacadeSavedData.get(serverLevel).put(pos, block);
            } else {
                CFClientEvents.CAMOUFLAGED_BLOCKS.put(pos, block);
            }

            if (!p_41427_.getPlayer().isCreative() && CFConfig.consumeFacade) {
                itemStack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }
        return super.useOn(p_41427_);
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        if (p_41458_.hasTag()) {
            CompoundTag tag = p_41458_.getTag();
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
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ClientStuff.FACADE_ITEM_RENDERER;
            }
        });
    }
}
