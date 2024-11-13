package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.CFClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
            String targetBlock = BuiltInRegistries.BLOCK.getKey(p_41427_.getLevel().getBlockState(p_41427_.getClickedPos()).getBlock()).toString();
            if(!CFConfig.blocks.contains(targetBlock)) {
                return InteractionResult.FAIL;
            }

            if (p_41427_.getLevel() instanceof ServerLevel serverLevel) {
                CableFacadeSavedData.get(serverLevel).put(pos, block);
            } else {
                CFClientEvents.CAMOUFLAGED_BLOCKS.put(pos, block);
            }
            if(!p_41427_.getPlayer().isCreative()) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(p_41427_);
    }
}
