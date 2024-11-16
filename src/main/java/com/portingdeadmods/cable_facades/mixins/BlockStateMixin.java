package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public class BlockStateMixin implements IForgeBlockState {
    @Override
    public BlockState getAppearance(BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        CFMain.LOGGER.debug("getting app");
        if (GameClientEvents.CAMOUFLAGED_BLOCKS.containsKey(pos)) {
            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(pos);
            if (camoBlock != null) {
                BlockState camoState = camoBlock.defaultBlockState();
                return camoState.getBlock().getAppearance(camoState, level, pos, side, queryState, queryPos);
            }
        }
        return queryState != null ? queryState : level.getBlockState(pos);
    }
}
