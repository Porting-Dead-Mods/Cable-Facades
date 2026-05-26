package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AddDirectionalFacadePacket(BlockPos pos, Direction face, BlockState state, ResourceLocation facadeType) {
    public static void encode(AddDirectionalFacadePacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        buf.writeByte(pkt.face.get3DDataValue());
        CodecUtils.writeBlockState(buf, pkt.state);
        buf.writeResourceLocation(pkt.facadeType);
    }

    public static AddDirectionalFacadePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction face = Direction.from3DDataValue(buf.readByte());
        BlockState state = CodecUtils.readBlockState(buf);
        ResourceLocation type = buf.readResourceLocation();
        return new AddDirectionalFacadePacket(pos, face, state, type);
    }

    public static void handle(AddDirectionalFacadePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFacadeManager.addDirectionalFacade(pkt.pos, pkt.face, pkt.state, pkt.facadeType);
            FacadeClientUtils.updateClientBlock(pkt.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
