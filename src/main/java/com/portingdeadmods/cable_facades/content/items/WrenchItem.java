package com.portingdeadmods.cable_facades.content.items;

import com.portingdeadmods.cable_facades.CFMain;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {
    public WrenchItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41427_) {
        BlockPos pos = p_41427_.getClickedPos();
        BlockState blockState = p_41427_.getLevel().getBlockState(pos);
        BlockState appearance = blockState.getAppearance(p_41427_.getLevel(), pos, p_41427_.getClickedFace(), blockState, pos);
        CFMain.LOGGER.debug("App: {}", appearance);
        return super.useOn(p_41427_);
    }
}
