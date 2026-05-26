package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveFacadePacket(BlockPos facadePos) {
    public static void encode(RemoveFacadePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.facadePos);
    }

    public static RemoveFacadePacket decode(FriendlyByteBuf buf) {
        return new RemoveFacadePacket(buf.readBlockPos());
    }

    public static void handle(RemoveFacadePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFacadeManager.remove(pkt.facadePos);
            FacadeClientUtils.updateClientBlock(pkt.facadePos);
        });
        ctx.get().setPacketHandled(true);
    }
}
