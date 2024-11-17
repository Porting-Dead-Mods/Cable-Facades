package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IForgeBlockState {

    @Override
    public BlockState getAppearance(BlockAndTintGetter blockGetter, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        if (ClientFacadeManager.FACADED_BLOCKS.containsKey(pos)) {
            Block camoBlock = ClientFacadeManager.FACADED_BLOCKS.get(pos);
            if (camoBlock != null) {
                BlockState camoState = camoBlock.defaultBlockState();
                return camoState.getBlock().getAppearance(camoState, blockGetter, pos, side, queryState, queryPos);
            }
        }
        return cable_facades$self().getBlock().getAppearance(cable_facades$self(), blockGetter, pos, side, queryState, queryPos);
    }


    @Override
    public int getLightEmission(BlockGetter blockGetter, BlockPos pos) {
        if (ClientFacadeManager.FACADED_BLOCKS.containsKey(pos)) {
            Block camoBlock = ClientFacadeManager.FACADED_BLOCKS.get(pos);
            if (camoBlock != null) {
                CFMain.LOGGER.debug("Self block state: {}", cable_facades$self());
                CFMain.LOGGER.debug("Camo block state: {}", ClientFacadeManager.FACADED_BLOCKS.get(pos));
                return camoBlock.getLightEmission(camoBlock.defaultBlockState(), blockGetter, pos);
            }
        }
        return cable_facades$self().getBlock().getLightEmission(cable_facades$self(), blockGetter, pos);
    }

    @Unique
    private BlockState cable_facades$self() {
        return (BlockState) (Object) this;
    }
}