package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.CFConfig;
import com.portingdeadmods.cable_facades.client.render.BlockModelLighterGetter;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererMixin implements BlockModelLighterGetter {
    @Shadow
    @Final
    private BlockModelLighter lighter;

    @Inject(
            method = "tesselateBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTesselateBlock(BlockQuadOutput output, float x, float y, float z, BlockAndTintGetter level, BlockPos pos, BlockState blockState, BlockStateModel model, long seed, CallbackInfo ci) {
        FacadeData data = FacadeUtils.getFacadeData(level, pos);
        if (data == null) return;

        if (data.isFullBlock() && CFConfig.shouldHideWhenFacaded(blockState.getBlock())) {
            ci.cancel();
        }
    }

    @Override
    public BlockModelLighter cable_facades$getLighter() {
        return lighter;
    }
}
