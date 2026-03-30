package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveDirectionalFacadePayload(BlockPos pos, Direction face) implements CustomPacketPayload {
    public static final Type<RemoveDirectionalFacadePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "remove_directional_facade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveDirectionalFacadePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            RemoveDirectionalFacadePayload::pos,
            ByteBufCodecs.VAR_INT.map(Direction::from3DDataValue, Direction::get3DDataValue),
            RemoveDirectionalFacadePayload::face,
            RemoveDirectionalFacadePayload::new
    );

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFacadeManager.removeDirectionalFacade(pos, face);
            FacadeUtils.updateClientBlock(pos);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
