package com.portingdeadmods.cable_facades.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public record FacadeData(@NotNull ResourceLocation facadeType,
                         @Nullable BlockState fullBlock,
                         @Nullable EnumMap<Direction, BlockState> directional) {

    private static final byte TYPE_FULL = 0;
    private static final byte TYPE_DIRECTIONAL = 1;

    public FacadeData(@Nullable BlockState fullBlock, @Nullable EnumMap<Direction, BlockState> directional) {
        this(FacadeTypes.DEFAULT_ID, fullBlock, directional);
    }

    public static FacadeData fullBlock(BlockState state) {
        return new FacadeData(FacadeTypes.DEFAULT_ID, state, null);
    }

    public static FacadeData fullBlock(ResourceLocation type, BlockState state) {
        return new FacadeData(type, state, null);
    }

    public static FacadeData directional(Direction face, BlockState state) {
        EnumMap<Direction, BlockState> map = new EnumMap<>(Direction.class);
        map.put(face, state);
        return new FacadeData(FacadeTypes.DEFAULT_ID, null, map);
    }

    public static FacadeData directional(ResourceLocation type, Direction face, BlockState state) {
        EnumMap<Direction, BlockState> map = new EnumMap<>(Direction.class);
        map.put(face, state);
        return new FacadeData(type, null, map);
    }

    public static FacadeData directional(EnumMap<Direction, BlockState> faces) {
        return new FacadeData(FacadeTypes.DEFAULT_ID, null, faces);
    }

    public static FacadeData directional(ResourceLocation type, EnumMap<Direction, BlockState> faces) {
        return new FacadeData(type, null, faces);
    }

    public boolean isFullBlock() {
        return fullBlock != null;
    }

    public boolean isDirectional() {
        return directional != null && !directional.isEmpty();
    }

    public boolean hasFace(Direction dir) {
        if (fullBlock != null) return true;
        return directional != null && directional.containsKey(dir);
    }

    @Nullable
    public BlockState getFace(Direction dir) {
        if (fullBlock != null) return fullBlock;
        return directional != null ? directional.get(dir) : null;
    }

    @Nullable
    public BlockState getFullBlock() {
        return fullBlock;
    }

    public FacadeData withFace(Direction dir, BlockState state) {
        EnumMap<Direction, BlockState> map = directional != null
                ? new EnumMap<>(directional)
                : new EnumMap<>(Direction.class);
        map.put(dir, state);

        if (map.size() == 6) {
            BlockState first = map.values().iterator().next();
            boolean allSame = map.values().stream().allMatch(s -> s == first);
            if (allSame) {
                return new FacadeData(facadeType, first, null);
            }
        }
        return new FacadeData(facadeType, null, map);
    }

    @Nullable
    public FacadeData withoutFace(Direction dir) {
        if (directional == null) return null;
        EnumMap<Direction, BlockState> map = new EnumMap<>(directional);
        map.remove(dir);
        if (map.isEmpty()) return null;
        return new FacadeData(facadeType, null, map);
    }

    private static final MapCodec<FacadeData> FULL_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.optionalFieldOf("facadeType", FacadeTypes.DEFAULT_ID).forGetter(FacadeData::facadeType),
            CodecUtils.BLOCKSTATE_CODEC.fieldOf("state").forGetter(FacadeData::fullBlock)
    ).apply(builder, FacadeData::fullBlock));

    private static final MapCodec<FacadeData> DIRECTIONAL_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.optionalFieldOf("facadeType", FacadeTypes.DEFAULT_ID).forGetter(FacadeData::facadeType),
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCKSTATE_CODEC).fieldOf("faces").forGetter(d -> {
                Map<String, BlockState> map = new HashMap<>();
                if (d.directional() != null) {
                    d.directional().forEach((dir, state) -> map.put(dir.getSerializedName(), state));
                }
                return map;
            })
    ).apply(builder, (type, map) -> {
        EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
        map.forEach((name, state) -> {
            Direction dir = Direction.byName(name);
            if (dir != null) faces.put(dir, state);
        });
        return FacadeData.directional(type, faces);
    }));

    public static final Codec<FacadeData> CODEC = Codec.STRING.dispatch(
            "type",
            data -> data.isFullBlock() ? "full" : "directional",
            type -> switch (type) {
                case "full" -> FULL_CODEC.codec();
                case "directional" -> DIRECTIONAL_CODEC.codec();
                default -> throw new IllegalArgumentException("Unknown facade type: " + type);
            }
    );

    public static void encode(FriendlyByteBuf buf, FacadeData data) {
        buf.writeResourceLocation(data.facadeType);
        if (data.isFullBlock()) {
            buf.writeByte(TYPE_FULL);
            CodecUtils.writeBlockState(buf, data.fullBlock);
        } else {
            buf.writeByte(TYPE_DIRECTIONAL);
            EnumMap<Direction, BlockState> faces = data.directional;
            int size = faces != null ? faces.size() : 0;
            buf.writeByte(size);
            if (faces != null) {
                faces.forEach((dir, state) -> {
                    buf.writeByte(dir.get3DDataValue());
                    CodecUtils.writeBlockState(buf, state);
                });
            }
        }
    }

    public static FacadeData decode(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        byte kind = buf.readByte();
        if (kind == TYPE_FULL) {
            return FacadeData.fullBlock(type, CodecUtils.readBlockState(buf));
        } else {
            int count = buf.readByte();
            EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
            for (int i = 0; i < count; i++) {
                Direction dir = Direction.from3DDataValue(buf.readByte());
                faces.put(dir, CodecUtils.readBlockState(buf));
            }
            return FacadeData.directional(type, faces);
        }
    }
}
