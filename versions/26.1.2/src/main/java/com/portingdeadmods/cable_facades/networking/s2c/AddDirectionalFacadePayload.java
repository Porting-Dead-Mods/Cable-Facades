package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddDirectionalFacadePayload(BlockPos pos, Direction face, BlockState state,
                                          Identifier facadeType) implements CustomPacketPayload {
    public static final Type<AddDirectionalFacadePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CFMain.MODID, "add_directional_facade"));

    public AddDirectionalFacadePayload(BlockPos pos, Direction face, BlockState state) {
        this(pos, face, state, FacadeTypes.DEFAULT_ID);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AddDirectionalFacadePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AddDirectionalFacadePayload::pos,
            ByteBufCodecs.VAR_INT.map(Direction::from3DDataValue, Direction::get3DDataValue),
            AddDirectionalFacadePayload::face,
            CodecUtils.BLOCKSTATE_STREAM_CODEC,
            AddDirectionalFacadePayload::state,
            Identifier.STREAM_CODEC,
            AddDirectionalFacadePayload::facadeType,
            AddDirectionalFacadePayload::new
    );

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFacadeManager.addDirectionalFacade(pos, face, state, facadeType);
            FacadeClientUtils.updateClientBlock(pos);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
