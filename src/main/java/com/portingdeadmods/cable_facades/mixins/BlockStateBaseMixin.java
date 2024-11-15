package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.networking.RemoveCamoPacket;
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
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow public abstract Block getBlock();

    @Inject(
            method = "onRemove",
            at = @At("HEAD")
    )
    private void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean isMoving, CallbackInfo ci) {
        Block block = getBlock();
        if (CFConfig.isBlockAllowed(block) && FacadeUtils.hasFacade(level, blockPos)) {
            if (!blockState.is(block)) {
                if (level instanceof ServerLevel serverLevel) {
                    CableFacadeSavedData data = CableFacadeSavedData.get(serverLevel);
                    Block camoBlock = data.getCamouflagedBlocks().get(blockPos);
                    ItemStack facadeStack = new ItemStack(CFItems.FACADE.get());
                    CompoundTag nbtData = new CompoundTag();
                    nbtData.putString("facade_block", BuiltInRegistries.BLOCK.getKey(camoBlock).toString());
                    facadeStack.setTag(nbtData);
                    data.remove(blockPos);
                    ModMessages.sendToClients(new RemoveCamoPacket(blockPos));
                    Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facadeStack);
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
        if (CFConfig.isBlockAllowed(getBlock())) {
            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(blockPos);
            if (camoBlock != null) {
                cir.setReturnValue(camoBlock.defaultBlockState().getCollisionShape(blockGetter, blockPos, collisionContext));
            }
        }
    }

    @Inject(
            method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getShape(BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext, CallbackInfoReturnable<VoxelShape> cir) {
        if (CFConfig.isBlockAllowed(getBlock())) {
            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(blockPos);
            if (camoBlock != null) {
                cir.setReturnValue(camoBlock.defaultBlockState().getShape(blockGetter, blockPos, collisionContext));
            }
        }
    }

    @Inject(
            method = "getOcclusionShape",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getOcclusionShape(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<VoxelShape> cir) {
        if (CFConfig.isBlockAllowed(getBlock())) {
            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(blockPos);
            if (camoBlock != null) {
                cir.setReturnValue(camoBlock.defaultBlockState().getOcclusionShape(blockGetter, blockPos));
            }
        }
    }

    @Inject(method = "isSolidRender", at = @At("HEAD"), cancellable = true)
    private void isSolidRender(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(blockPos);
        if (camoBlock != null) {
            BlockState camoState = camoBlock.defaultBlockState();
            boolean result = camoState.canOcclude() && Block.isShapeFullBlock(camoState.getOcclusionShape(blockGetter, blockPos));
            System.out.println("isSolidRender: " + result +" for block: " + camoBlock + " so the result was changed for block: " + getBlock());
            cir.setReturnValue(result);
        }
    }

    @Unique
    private static final ThreadLocal<Boolean> RECURSION_GUARD = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "getLightBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getLightBlock(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
        if (RECURSION_GUARD.get()) return;

        try {
            RECURSION_GUARD.set(true);

            Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(blockPos);
            if (camoBlock != null) {
                BlockState camoBlockState = camoBlock.defaultBlockState();
                cir.setReturnValue(camoBlockState.getLightBlock(blockGetter, blockPos));
            }
        } finally {
            RECURSION_GUARD.set(false);
        }
    }

    @Inject(
            method = "propagatesSkylightDown(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void propagatesSkylightDown(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        Block camoBlock = GameClientEvents.CAMOUFLAGED_BLOCKS.get(blockPos);
        if (camoBlock != null) {
            cir.setReturnValue(camoBlock.defaultBlockState().propagatesSkylightDown(blockGetter, blockPos));
        }
    }
}
