package com.portingdeadmods.cable_facades.api.facade_type;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public record FacadeType(
        ResourceLocation id,
        Predicate<BlockState> canApplyOn,
        ResourceLocation outlineModel
) {
}
