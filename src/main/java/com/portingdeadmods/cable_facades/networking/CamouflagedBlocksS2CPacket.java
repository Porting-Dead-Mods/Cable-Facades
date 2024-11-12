package com.portingdeadmods.cable_facades.networking;

import com.portingdeadmods.cable_facades.events.CFClientEvents;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class CamouflagedBlocksS2CPacket {
    private final Object2ObjectOpenHashMap<BlockPos, Block> camouflagedBlocks;

    public CamouflagedBlocksS2CPacket(Object2ObjectOpenHashMap<BlockPos, Block> camouflagedBlocks) {
        this.camouflagedBlocks = camouflagedBlocks;
    }

    public CamouflagedBlocksS2CPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.camouflagedBlocks = new Object2ObjectOpenHashMap<>(size);
        for (int i = 0; i < size; i++) {
            this.camouflagedBlocks.put(buf.readBlockPos(), BuiltInRegistries.BLOCK.get(buf.readResourceLocation()));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.camouflagedBlocks.size());
        for (Map.Entry<BlockPos, Block> entry : this.camouflagedBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(entry.getValue()));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            CFClientEvents.CAMOUFLAGED_BLOCKS = new Object2ObjectOpenHashMap<>(this.camouflagedBlocks);
        });
        return true;
    }
}
