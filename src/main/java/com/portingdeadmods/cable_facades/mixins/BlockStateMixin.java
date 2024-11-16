package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.events.ClientCamoManager;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IForgeBlockState {

    @Shadow protected abstract BlockState asState();

    @Override
    public BlockState getAppearance(BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        if (ClientCamoManager.CAMOUFLAGED_BLOCKS.containsKey(pos)) {
            Block camoBlock = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos);
            if (camoBlock != null) {
                BlockState camoState = camoBlock.defaultBlockState();
                return camoState.getBlock().getAppearance(camoState, level, pos, side, queryState, queryPos);
            }
        }
        return cable_facades$self().getBlock().getAppearance(cable_facades$self(), level, pos, side, queryState, queryPos);
    }

    @Override
    public int getLightEmission(BlockGetter level, BlockPos pos) {

        if (GameClientEvents.CAMOUFLAGED_BLOCKS.containsKey(pos)) {
            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(pos);
            if (camoBlock != null) {
                BlockState camoState = camoBlock.defaultBlockState();
                return camoState.getLightEmission();
            }
        }
        return cable_facades$self().getBlock().getLightEmission(cable_facades$self(), level, pos);
    }

    @Unique
    private BlockState cable_facades$self() {
        return (BlockState) (Object) this;
    }
}
