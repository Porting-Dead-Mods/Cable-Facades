package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AddFacadePacket(BlockPos blockPos, Block block) {
    public AddFacadePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), BuiltInRegistries.BLOCK.get(buf.readResourceLocation()));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientFacadeManager.FACADED_BLOCKS.put(blockPos, block));
        return true;
    }
}
