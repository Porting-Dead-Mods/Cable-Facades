package com.portingdeadmods.cable_facades.mixins;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    /*
    @Inject(
            method = "getLightBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getLightBlock(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(pos);
        if (camoBlock != null) {
            BlockState facadeState = camoBlock.defaultBlockState();
            cir.setReturnValue(facadeState.getLightBlock(level, pos));
            }
        }
     */
    }




