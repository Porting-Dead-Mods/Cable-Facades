package com.portingdeadmods.cable_facades.mixins;

import ca.weblite.objc.Client;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IForgeBlockState {

    @Shadow protected abstract BlockState asState();

    @Override
    public BlockState getAppearance(BlockAndTintGetter blockGetter, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        if (ClientFacadeManager.FACADED_BLOCKS.containsKey(pos)) {
            Block camoBlock = ClientFacadeManager.FACADED_BLOCKS.get(pos);
            if (camoBlock != null) {
                BlockState camoState = camoBlock.defaultBlockState();
                return camoState.getBlock().getAppearance(camoState, blockGetter, pos, side, queryState, queryPos);
            }
        }
        return cable_facades$self().getBlock().getAppearance(cable_facades$self(), blockGetter, pos, side, queryState, queryPos);
    }

    @Override
    public int getLightEmission(BlockGetter blockGetter, BlockPos pos) {
        if (FacadeUtils.hasFacade(blockGetter,pos)) {
            Block camoBlock = FacadeUtils.getFacade(blockGetter,pos);
            if (camoBlock != null) {
                System.out.println("This = "+this.asState().getBlock());
                System.out.println("Facade = "+camoBlock);
                return camoBlock.defaultBlockState().getLightEmission();
            }
        }
        return cable_facades$self().getBlock().getLightEmission(cable_facades$self(), blockGetter, pos);
    }


    @Unique
    private BlockState cable_facades$self() {
        return (BlockState) (Object) this;
    }
}