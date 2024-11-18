package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IBlockStateExtension {
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
                Block camoBlock = ClientFacadeManager.FACADED_BLOCKS.get(pos);
                if (camoBlock != null) {
                    BlockState camoState = camoBlock.defaultBlockState();
                    return camoState.getBlock().getAppearance(camoState, blockGetter, pos, side, queryState, queryPos);
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
                Block camoBlock = FacadeUtils.getFacade(blockGetter, pos);
                if (camoBlock != null) {
                    return camoBlock.defaultBlockState().getLightEmission();
                }
            }
            return getBlock().getLightEmission(this.asState(), blockGetter, pos);
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }
}
