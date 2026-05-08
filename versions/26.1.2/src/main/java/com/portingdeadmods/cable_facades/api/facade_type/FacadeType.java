package com.portingdeadmods.cable_facades.api.facade_type;

import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public record FacadeType(
        Identifier id,
        Predicate<BlockState> canApplyOn,
        Identifier outlineModel,
        @Nullable Supplier<FacadeItem> fullItem,
        @Nullable Supplier<DirectionalFacadeItem> directionalItem
) {
    public FacadeType(Identifier id, Predicate<BlockState> canApplyOn, Identifier outlineModel) {
        this(id, canApplyOn, outlineModel, null, null);
    }
}
