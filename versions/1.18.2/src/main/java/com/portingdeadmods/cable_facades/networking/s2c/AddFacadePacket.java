package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AddFacadePacket(BlockPos facadePos, FacadeData facadeData) {
    public static void encode(AddFacadePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.facadePos);
        FacadeData.encode(buf, pkt.facadeData);
    }

    public static AddFacadePacket decode(FriendlyByteBuf buf) {
        return new AddFacadePacket(buf.readBlockPos(), FacadeData.decode(buf));
    }

    public static void handle(AddFacadePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFacadeManager.put(pkt.facadePos, pkt.facadeData);
            FacadeClientUtils.updateClientBlock(pkt.facadePos);
        });
        ctx.get().setPacketHandled(true);
    }
}
