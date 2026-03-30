package com.portingdeadmods.cable_facades.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public record FacadeData(@Nullable BlockState fullBlock,
                         @Nullable EnumMap<Direction, BlockState> directional) {

    private static final byte TYPE_FULL = 0;
    private static final byte TYPE_DIRECTIONAL = 1;

    public static FacadeData fullBlock(BlockState state) {
        return new FacadeData(state, null);
    }

    public static FacadeData directional(Direction face, BlockState state) {
        EnumMap<Direction, BlockState> map = new EnumMap<>(Direction.class);
        map.put(face, state);
        return new FacadeData(null, map);
    }

    public static FacadeData directional(EnumMap<Direction, BlockState> faces) {
        return new FacadeData(null, faces);
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
        EnumMap<Direction, BlockState> map;
        if (directional != null) {
            map = new EnumMap<>(directional);
        } else {
            map = new EnumMap<>(Direction.class);
        }
        map.put(dir, state);

        if (map.size() == 6) {
            BlockState first = map.values().iterator().next();
            boolean allSame = map.values().stream().allMatch(s -> s == first);
            if (allSame) {
                return fullBlock(first);
            }
        }

        return new FacadeData(null, map);
    }

    @Nullable
    public FacadeData withoutFace(Direction dir) {
        if (directional == null) return null;
        EnumMap<Direction, BlockState> map = new EnumMap<>(directional);
        map.remove(dir);
        if (map.isEmpty()) return null;
        return new FacadeData(null, map);
    }

    private static final MapCodec<FacadeData> FULL_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            CodecUtils.BLOCKSTATE_CODEC.fieldOf("state").forGetter(d -> d.fullBlock())
    ).apply(builder, FacadeData::fullBlock));

    private static final MapCodec<FacadeData> DIRECTIONAL_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCKSTATE_CODEC).fieldOf("faces").forGetter(d -> {
                Map<String, BlockState> map = new java.util.HashMap<>();
                if (d.directional() != null) {
                    d.directional().forEach((dir, state) -> map.put(dir.getSerializedName(), state));
                }
                return map;
            })
    ).apply(builder, map -> {
        EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
        map.forEach((name, state) -> {
            Direction dir = Direction.byName(name);
            if (dir != null) faces.put(dir, state);
        });
        return FacadeData.directional(faces);
    }));

    public static final Codec<FacadeData> CODEC = Codec.STRING.fieldOf("type").codec().dispatch(
            data -> data.isFullBlock() ? "full" : "directional",
            type -> switch (type) {
                case "full" -> FULL_CODEC;
                case "directional" -> DIRECTIONAL_CODEC;
                default -> throw new IllegalArgumentException("Unknown facade type: " + type);
            }
    );

    public static final Codec<FacadeData> MIGRATION_CODEC = CodecUtils.BLOCKSTATE_CODEC.xmap(
            FacadeData::fullBlock,
            data -> data.fullBlock() != null ? data.fullBlock() : null
    );

    public static final StreamCodec<ByteBuf, FacadeData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FacadeData decode(ByteBuf buf) {
            byte type = buf.readByte();
            if (type == TYPE_FULL) {
                BlockState state = CodecUtils.BLOCKSTATE_STREAM_CODEC.decode(buf);
                return FacadeData.fullBlock(state);
            } else {
                int count = buf.readByte();
                EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
                for (int i = 0; i < count; i++) {
                    Direction dir = Direction.from3DDataValue(buf.readByte());
                    BlockState state = CodecUtils.BLOCKSTATE_STREAM_CODEC.decode(buf);
                    faces.put(dir, state);
                }
                return FacadeData.directional(faces);
            }
        }

        @Override
        public void encode(ByteBuf buf, FacadeData data) {
            if (data.isFullBlock()) {
                buf.writeByte(TYPE_FULL);
                CodecUtils.BLOCKSTATE_STREAM_CODEC.encode(buf, data.fullBlock());
            } else {
                buf.writeByte(TYPE_DIRECTIONAL);
                EnumMap<Direction, BlockState> faces = data.directional();
                buf.writeByte(faces != null ? faces.size() : 0);
                if (faces != null) {
                    faces.forEach((dir, state) -> {
                        buf.writeByte(dir.get3DDataValue());
                        CodecUtils.BLOCKSTATE_STREAM_CODEC.encode(buf, state);
                    });
                }
            }
        }
    };
}
