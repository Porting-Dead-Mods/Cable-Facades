package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveDirectionalFacadePacket(BlockPos pos, Direction face) {
    public static void encode(RemoveDirectionalFacadePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeByte(pkt.face.get3DDataValue());
    }

    public static RemoveDirectionalFacadePacket decode(FriendlyByteBuf buf) {
        return new RemoveDirectionalFacadePacket(buf.readBlockPos(), Direction.from3DDataValue(buf.readByte()));
    }

    public static void handle(RemoveDirectionalFacadePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFacadeManager.removeDirectionalFacade(pkt.pos, pkt.face);
            FacadeClientUtils.updateClientBlock(pkt.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
