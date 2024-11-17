package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.ClientCamoManager;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadePacket;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

    @Shadow public abstract Block getBlock();

    @Unique
    private static final ThreadLocal<Boolean> RECURSION_GUARD = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "onRemove",
            at = @At("HEAD")
    )
    private void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean isMoving, CallbackInfo ci) {
        if (FacadeUtils.hasFacade(level, blockPos)) {
            if (!blockState.is(getBlock())) {
                if (level instanceof ServerLevel serverLevel) {
                    CableFacadeSavedData data = CableFacadeSavedData.get(serverLevel);
                    Block camoBlock = data.getCamouflagedBlocks().get(blockPos);
                    if (camoBlock != null) {
                        ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
                        CompoundTag nbtData = new CompoundTag();
                        nbtData.putString("facade_block", BuiltInRegistries.BLOCK.getKey(camoBlock).toString());
                        facadeStack.setTag(nbtData);
                        data.remove(blockPos);
                        ModMessages.sendToClients(new RemoveFacadePacket(blockPos));
                        Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facadeStack);
                    }
                }
            }
        }
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getCollisionShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (!RECURSION_GUARD.get()) {
            try {
                RECURSION_GUARD.set(true);
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getCollisionShape(blockGetter, blockPos, collisionContext));
                }
            } finally {
                RECURSION_GUARD.set(false);
            }
        }
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext,CallbackInfoReturnable<VoxelShape> cir) {
        if (!RECURSION_GUARD.get()) {
            try {
                RECURSION_GUARD.set(true);
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getShape(blockGetter, blockPos, collisionContext));
                }
            } finally {
                RECURSION_GUARD.set(false);
            }
        }
    }

    @Inject(
            method = "getOcclusionShape",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<VoxelShape> cir) {
        if (!RECURSION_GUARD.get()) {
            try {
                RECURSION_GUARD.set(true);
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    cir.setReturnValue(camoBlock.defaultBlockState().getOcclusionShape(blockGetter, blockPos));
                }
            } finally {
                RECURSION_GUARD.set(false);
            }
        }
    }


    @Inject(
            method = "isSolidRender",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isSolidRender(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (!RECURSION_GUARD.get()) {
            try {
                RECURSION_GUARD.set(true);
                Block camoBlock = FacadeUtils.getFacade(blockGetter, blockPos);
                if (camoBlock != null) {
                    BlockState camoState = camoBlock.defaultBlockState();
                    cir.setReturnValue(camoState.canOcclude() && camoState.isSolidRender(blockGetter, blockPos));
                }
            } finally {
                RECURSION_GUARD.set(false);
            }
        }
    }

    @Inject(
            method = "getLightBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getLightBlock(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (!RECURSION_GUARD.get()) {
            try {
                RECURSION_GUARD.set(true);
                Block camoBlock = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos);
                if (camoBlock != null) {
                    BlockState camoState = camoBlock.defaultBlockState();
                    if (camoState.isSolidRender(level, pos)) {
                        cir.setReturnValue(level.getMaxLightLevel());
                    } else {
                        cir.setReturnValue(camoState.propagatesSkylightDown(level, pos) ? 0 : 1);
                    }
                }
            } finally {
                RECURSION_GUARD.set(false);
            }
        }
    }

    @Inject(
            method = "propagatesSkylightDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void propagatesSkylightDown(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!RECURSION_GUARD.get()) {
            try {
                RECURSION_GUARD.set(true);
                Block camoBlock = ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos);
                if (camoBlock != null) {
                    BlockState camoState = camoBlock.defaultBlockState();
                    cir.setReturnValue(camoBlock.propagatesSkylightDown(camoState, level, pos));
                }
            } finally {
                RECURSION_GUARD.set(false);
            }
        }
    }
}