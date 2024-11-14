package com.portingdeadmods.cable_facades.mixins;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.networking.RemoveCamoPacket;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow public abstract Block getBlock();

    @Inject(
            method = "onRemove",
            at = @At("HEAD")
    )
    private void onRemove(Level level, BlockPos blockPos, BlockState blockState, boolean isMoving, CallbackInfo ci) {
        Block block = getBlock();
        if (CFConfig.isBlockAllowed(block)) {
            CFMain.LOGGER.debug("pipe block");
            if (!blockState.is(block)) {
                CFMain.LOGGER.debug("removing camo block");
                if (level instanceof ServerLevel serverLevel) {
                    CableFacadeSavedData.get(serverLevel).remove(blockPos);
                    ModMessages.sendToClients(new RemoveCamoPacket(blockPos));
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
}
