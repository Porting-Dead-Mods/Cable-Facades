package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow
    public abstract Block getBlock();

    @Unique
    private static final ThreadLocal<Boolean> cable_facades$recursionGuard = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onRemove",
            at = @At("HEAD")
    )
    private void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean isMoving, CallbackInfo ci) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(level, blockPos)) {
                if (!blockState.is(getBlock())) {
                    if (level instanceof ServerLevel serverLevel) {
                        CableFacadeSavedData data = CableFacadeSavedData.get(serverLevel);
                        Block facadeBlock = data.getFacade(blockPos);
                        if (facadeBlock != null) {
                            ItemStack facadeStack = CFItems.FACADE.get().createFacade(facadeBlock);
                            FacadeUtils.removeFacade(level, blockPos);

                            Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facadeStack);
                        }
                    }
                    FacadeUtils.updateBlocks(level, blockPos);
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, blockPos)) {
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getCollisionShape(blockGetter, BlockPos.ZERO, collisionContext));
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, blockPos)) {
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getShape(blockGetter, BlockPos.ZERO, collisionContext));
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Inject(
            method = "getOcclusionShape",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<VoxelShape> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, blockPos)) {
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getOcclusionShape(blockGetter, BlockPos.ZERO));
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Inject(
            method = "getLightBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getLightBlock(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, blockPos)) {
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getLightBlock(blockGetter, BlockPos.ZERO));
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Inject(
            method = "propagatesSkylightDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, blockPos)) {
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().propagatesSkylightDown(blockGetter, BlockPos.ZERO));
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Inject(
            method = "isSolidRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isSolidRender(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, blockPos)) {
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().isSolidRender(blockGetter, BlockPos.ZERO));
                }
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }
}
