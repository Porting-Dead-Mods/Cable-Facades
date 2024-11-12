package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.events.CFClientEvents;
import de.maxhenkel.pipez.blocks.PipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PipeBlock.class)
public class PipezBlockMixin {
    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void onRemoveBlock(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo ci) {
        if (!state.is(newState.getBlock())) {
            CFClientEvents.CAMOUFLAGED_BLOCKS.remove(pos);
        }
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getBlockShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        Block block = CFClientEvents.CAMOUFLAGED_BLOCKS.get(pos);
        if (block != null) {
            cir.setReturnValue(Shapes.or(block.defaultBlockState().getShape(worldIn, pos, context), cir.getReturnValue()));
        }
    }

    @Inject(
            method = "getCollisionShape",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getCollisionBlockShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        Block block = CFClientEvents.CAMOUFLAGED_BLOCKS.get(pos);
        if (block != null) {
            cir.setReturnValue(Shapes.or(block.defaultBlockState().getShape(worldIn, pos, context), cir.getReturnValue()));
        }
    }
}
