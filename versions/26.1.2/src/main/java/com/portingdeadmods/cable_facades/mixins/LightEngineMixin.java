package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin {

    @Final
    @Shadow
    protected LightChunkGetter chunkSource;

    @Unique
    private static final ThreadLocal<Boolean> cable_facades$recursionGuard = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getState", at = @At("RETURN"), cancellable = true)
    private void cable_facades$facadeAwareGetState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (cable_facades$recursionGuard.get()) return;
        cable_facades$recursionGuard.set(true);
        try {
            BlockGetter level = chunkSource.getLevel();
            FacadeData data = FacadeUtils.getFacadeData(level, pos);
            if (data == null) return;
            BlockState cableState = cir.getReturnValue();
            if (CFConfig.isScaleUpBlock(cableState.getBlock())) return;
            if (data.isFullBlock()) {
                cir.setReturnValue(data.getFullBlock());
            }
        } finally {
            cable_facades$recursionGuard.set(false);
        }
    }
}
