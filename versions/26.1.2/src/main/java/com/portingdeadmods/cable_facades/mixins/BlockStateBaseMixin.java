package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.multipart.MultipartShapeBuilder;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
            method = "affectNeighborsAfterRemoval",
            at = @At("HEAD")
    )
    private void cable_facades$affectNeighborsAfterRemoval(ServerLevel level, BlockPos blockPos, boolean movedByPiston, CallbackInfo ci) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            FacadeData facadeData = FacadeUtils.getFacadeData(level, blockPos);
            if (facadeData != null) {
                if (facadeData.isFullBlock()) {
                    FacadeItem fullItem = FacadeTypes.fullItemFor(facadeData.facadeType());
                    if (fullItem == null) fullItem = CFItems.FACADE.get();
                    ItemStack facadeStack = fullItem.createFacade(facadeData.getFullBlock().getBlock());
                    FacadeUtils.removeFacade(level, blockPos);
                    if (CFConfig.consumeFacade) Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), facadeStack);
                } else if (facadeData.isDirectional()) {
                    Identifier typeId = facadeData.facadeType();
                    DirectionalFacadeItem directionalLookup = FacadeTypes.directionalItemFor(typeId);
                    final DirectionalFacadeItem directionalItem = directionalLookup != null ? directionalLookup : CFItems.DIRECTIONAL_FACADE.get();
                    FacadeUtils.removeFacade(level, blockPos);
                    if (CFConfig.consumeFacade) {
                        facadeData.directional().forEach((dir, state) -> {
                            ItemStack stack = directionalItem.createFacade(state.getBlock());
                            Containers.dropItemStack(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), stack);
                        });
                    }
                }
                FacadeUtils.updateBlocks(level, blockPos);
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
            FacadeData facadeData = FacadeUtils.getFacadeData(blockGetter, blockPos);
            if (facadeData != null) {
                BlockState self = (BlockState) (Object) this;
                cir.setReturnValue(MultipartShapeBuilder.buildCollisionShape(blockGetter, blockPos, self, facadeData, collisionContext));
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
            FacadeData facadeData = FacadeUtils.getFacadeData(blockGetter, blockPos);
            if (facadeData != null) {
                BlockState self = (BlockState) (Object) this;
                cir.setReturnValue(MultipartShapeBuilder.buildShape(blockGetter, blockPos, self, facadeData, collisionContext));
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

}
