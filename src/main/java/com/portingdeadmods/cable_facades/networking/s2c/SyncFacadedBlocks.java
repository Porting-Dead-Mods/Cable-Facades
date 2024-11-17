package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record SyncFacadedBlocks(Map<BlockPos, Block> facadedBlocks) {
    public SyncFacadedBlocks(FriendlyByteBuf buf) {
        this(getFacades(buf));
    }

    public static Map<BlockPos, Block> getFacades(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<BlockPos, Block> facades = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            facades.put(buf.readBlockPos(), BuiltInRegistries.BLOCK.get(buf.readResourceLocation()));
        }
        return facades;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.facadedBlocks.size());
        for (Map.Entry<BlockPos, Block> entry : this.facadedBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(entry.getValue()));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientFacadeManager.FACADED_BLOCKS = this.facadedBlocks);
    }
}
