package com.portingdeadmods.cable_facades.registries;

import com.mojang.serialization.Codec;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class CFDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CFMain.MODID);

    public static final Codec<Optional<Block>> OPTIONAL_BLOCK_CODEC = CodecUtils.BLOCK_CODEC.optionalFieldOf("facade_block").codec();

    public static final Supplier<DataComponentType<Optional<Block>>> FACADE_BLOCK = DATA_COMPONENTS.registerComponentType("facade_block",
            blockBuilder -> blockBuilder.persistent(OPTIONAL_BLOCK_CODEC).networkSynchronized(ByteBufCodecs.optional(CodecUtils.BLOCK_STREAM_CODEC)));
}
