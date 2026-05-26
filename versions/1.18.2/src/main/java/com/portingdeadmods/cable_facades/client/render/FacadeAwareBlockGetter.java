package com.portingdeadmods.cable_facades.client.render;

import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FacadeAwareBlockGetter implements BlockAndTintGetter {
    private final BlockAndTintGetter delegate;

    public FacadeAwareBlockGetter(BlockAndTintGetter delegate) {
        this.delegate = delegate;
    }

    public BlockAndTintGetter getDelegate() {
        return delegate;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        FacadeData data = ClientFacadeManager.get(pos);
        if (data != null && data.isFullBlock()) {
            return data.getFullBlock();
        }
        return delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return delegate.getFluidState(pos);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return delegate.getBlockEntity(pos);
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return delegate.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
        return delegate.getBlockTint(pos, colorResolver);
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return delegate.getMinBuildHeight();
    }
}
