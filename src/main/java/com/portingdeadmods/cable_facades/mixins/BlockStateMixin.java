package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IForgeBlockState.class)
public abstract class BlockStateMixin {

    @Inject(
            method = "getAppearance",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    void onGetAppearance(BlockAndTintGetter level, BlockPos pos, Direction side, BlockState queryState, BlockPos queryPos, CallbackInfoReturnable<BlockState> cir) {
        if (GameClientEvents.CAMOUFLAGED_BLOCKS.containsKey(pos)) {
            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(pos);
            assert camoBlock != null;
            BlockState camoState = camoBlock.defaultBlockState();
            cir.setReturnValue(camoState.getBlock().getAppearance(camoState, level, pos, side, queryState, queryPos));
        }
    }
}
