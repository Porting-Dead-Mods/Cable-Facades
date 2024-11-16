package com.portingdeadmods.cable_facades.networking;

import com.portingdeadmods.cable_facades.events.ClientCamoManager;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveCamoPacket {
    private final BlockPos camoPos;

    public RemoveCamoPacket(BlockPos camoPos) {
        this.camoPos = camoPos;
    }

    public RemoveCamoPacket(FriendlyByteBuf buf) {
        this.camoPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.camoPos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientCamoManager.CAMOUFLAGED_BLOCKS.remove(this.camoPos));
    }
}