package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.events.ClientCamoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IForgeBlockState {

    @Override
    public BlockState getAppearance(BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        Block camoBlock = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos);
        if (camoBlock != null) {
            BlockState camoState = getCamoState(camoBlock);
            return camoState.getBlock().getAppearance(camoState, level, pos, side, queryState, queryPos);
        }

        BlockState self = cable_facades$self();
        return self.getBlock().getAppearance(self, level, pos, side, queryState, queryPos);
    }

    @Override
    public int getLightEmission(BlockGetter level, BlockPos pos) {
        Block camoBlock = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos);
        if (camoBlock != null) {
            return getCamoState(camoBlock).getLightEmission();
        }

        BlockState self = cable_facades$self();
        return self.getBlock().getLightEmission(self, level, pos);
    }

    @Unique
    private BlockState cable_facades$self() {
        return (BlockState) (Object) this;
    }

    @Unique
    private static BlockState getCamoState(Block block) {
        return block.defaultBlockState();
    }
}