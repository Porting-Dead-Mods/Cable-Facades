package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IForgeBlockState {
    @Shadow protected abstract BlockState asState();

    // NEVER CONSTRUCT THIS
    private BlockStateMixin() {
        super(null, null, null);
    }

    @Unique
    private static final ThreadLocal<Boolean> cable_facades$recursionGuard = ThreadLocal.withInitial(() -> false);

    @Override
    public BlockState getAppearance(BlockAndTintGetter blockGetter, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        if (cable_facades$recursionGuard.get()) return getBlock().getAppearance(this.asState(), blockGetter, pos, side, queryState, queryPos);
        cable_facades$recursionGuard.set(true);
        try {
            if (ClientFacadeManager.FACADED_BLOCKS.containsKey(pos)) {
                BlockState facadeState = ClientFacadeManager.FACADED_BLOCKS.get(pos);
                if (facadeState != null) {
                    return facadeState.getBlock().getAppearance(facadeState, blockGetter, pos, side, queryState, queryPos);
                }
            }
            return getBlock().getAppearance(this.asState(), blockGetter, pos, side, queryState, queryPos);
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Override
    public int getLightEmission(BlockGetter blockGetter, BlockPos pos) {
        if (cable_facades$recursionGuard.get()) return getBlock().getLightEmission(this.asState(), blockGetter, pos);
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(blockGetter, pos)) {
                BlockState facadeState = FacadeUtils.getFacade(blockGetter, pos);
                if (facadeState != null) {
                    return facadeState.getLightEmission();
                }
            }
            return getBlock().getLightEmission(this.asState(), blockGetter, pos);
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Override
    public boolean isFaceSturdy(BlockGetter level, BlockPos pos, Direction direction, SupportType supportType) {
        if (cable_facades$recursionGuard.get()) return super.isFaceSturdy(level, pos, direction, supportType);
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(level, pos)) {
                BlockState facadeState = FacadeUtils.getFacade(level, pos);
                if (facadeState != null) {
                    return facadeState.isFaceSturdy(level, BlockPos.ZERO, direction, supportType);
                }
            }
            return super.isFaceSturdy(level, pos, direction, supportType);
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }

    @Override
    public VoxelShape getVisualShape(BlockGetter level, BlockPos pos, CollisionContext context) {
        if (cable_facades$recursionGuard.get()) return super.getVisualShape(level, pos, context);
        cable_facades$recursionGuard.set(true);
        try {
            if (FacadeUtils.hasFacade(level, pos)) {
                BlockState facadeState = FacadeUtils.getFacade(level, pos);
                if (facadeState != null) {
                    return facadeState.getVisualShape(level, BlockPos.ZERO, context);
                }
            }
            return super.getVisualShape(level, pos, context);
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }
}
