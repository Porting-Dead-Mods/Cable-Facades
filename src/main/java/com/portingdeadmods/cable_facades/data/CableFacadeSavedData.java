package com.portingdeadmods.cable_facades.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.portingdeadmods.cable_facades.CFMain;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CableFacadeSavedData extends SavedData {
    public static final String ID = "framify_framed_block_saved_data";
    public static final Codec<Block> BLOCK_CODEC = ResourceLocation.CODEC.xmap(BuiltInRegistries.BLOCK::get, BuiltInRegistries.BLOCK::getKey);
    public static final Codec<Map<String, Block>> CODEC = Codec.unboundedMap(
            Codec.STRING,
            BLOCK_CODEC
    );

    private final Object2ObjectOpenHashMap<BlockPos, Block> camouflagedBlocks;

    public CableFacadeSavedData(Object2ObjectOpenHashMap<BlockPos, Block> framedBlocks) {
        this.camouflagedBlocks = framedBlocks;
    }

    public CableFacadeSavedData() {
        this.camouflagedBlocks = new Object2ObjectOpenHashMap<>();
    }

    public void put(BlockPos blockPos, Block block) {
        this.camouflagedBlocks.put(blockPos, block);
        setDirty();
    }

    public void remove(BlockPos blockPos) {
        this.camouflagedBlocks.remove(blockPos);
        setDirty();
    }

    public boolean contains(BlockPos blockPos) {
        return this.camouflagedBlocks.containsKey(blockPos) && this.camouflagedBlocks.get(blockPos) != null;
    }

    public Object2ObjectOpenHashMap<BlockPos, Block> getCamouflagedBlocks() {
        return camouflagedBlocks;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        DataResult<Tag> tagDataResult = CODEC.encodeStart(NbtOps.INSTANCE, blocksToString());
        tagDataResult
                .resultOrPartial(err -> CFMain.LOGGER.error("Encoding error: {}", err))
                .ifPresent(tag -> compoundTag.put(ID, tag));
        return compoundTag;
    }

    public Map<String, Block> blocksToString() {
        Map<String, Block> map = new HashMap<>();
        for (Map.Entry<BlockPos, Block> entry : camouflagedBlocks.entrySet()) {
            map.put(String.valueOf(entry.getKey().asLong()), entry.getValue());
        }
        return map;
    }

    public static CableFacadeSavedData load(CompoundTag tag) {
        DataResult<Pair<Map<String, Block>, Tag>> dataResult = CODEC.decode(NbtOps.INSTANCE, tag.get(ID));
        Optional<Pair<Map<String, Block>, Tag>> mapTagPair = dataResult
                .resultOrPartial(err -> CFMain.LOGGER.error("Decoding error: {}", err));
        if (mapTagPair.isPresent()) {
            Map<String, Block> map = mapTagPair.get().getFirst();
            return new CableFacadeSavedData(blocksFromString(map));
        }
        return new CableFacadeSavedData();
    }

    public static Object2ObjectOpenHashMap<BlockPos, Block> blocksFromString(Map<String, Block> map) {
        Object2ObjectOpenHashMap<BlockPos, Block> blocks = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<String, Block> entry : map.entrySet()) {
            blocks.put(BlockPos.of(Long.parseLong(entry.getKey())), entry.getValue());
        }
        return blocks;
    }

    public static CableFacadeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CableFacadeSavedData::load, CableFacadeSavedData::new, ID);
    }
}