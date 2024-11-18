package com.portingdeadmods.cable_facades.mixins;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockState.class)
public abstract class BlockStateMixin extends BlockBehaviour.BlockStateBase implements IForgeBlockState {
    // NEVER CONSTRUCT THIS
    private BlockStateMixin() {
        super(null, null, null);
    }

    @Override
    public BlockState getAppearance(BlockAndTintGetter blockGetter, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        if (ClientFacadeManager.FACADED_BLOCKS.containsKey(pos)) {
            Block camoBlock = ClientFacadeManager.FACADED_BLOCKS.get(pos);
            if (camoBlock != null) {
                BlockState camoState = camoBlock.defaultBlockState();
                return camoState.getBlock().getAppearance(camoState, blockGetter, pos, side, queryState, queryPos);
            }
        }
        BlockState blockState = blockGetter.getBlockState(pos);
        return getBlock().getAppearance(blockState, blockGetter, pos, side, queryState, queryPos);
    }

    @Override
    public int getLightEmission(BlockGetter blockGetter, BlockPos pos) {
        if (FacadeUtils.hasFacade(blockGetter,pos)) {
            Block camoBlock = FacadeUtils.getFacade(blockGetter,pos);
            if (camoBlock != null) {
                return camoBlock.defaultBlockState().getLightEmission();
            }
        }
        return getBlock().getLightEmission(this.asState(), blockGetter, pos);
    }
}