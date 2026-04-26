package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.html.Option;
import java.util.*;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Unique
    private static final String FACADES_TAG = "cable_facades";

    @Unique
    private final Map<BlockPos, FacadeData> facadeMap = new HashMap<>();

    @Inject(
            method = "fillFromWorld",
            at = @At("RETURN")
    )
    private void onFillFromWorld(Level level, BlockPos position, Vec3i size, boolean inludeEntities, List<Block> ignoreBlocks, CallbackInfo ci) {
        facadeMap.clear();

        BlockPos.betweenClosed(position, position.offset(size).offset(-1, -1, -1)).forEach(blockPos -> {
            FacadeData data = FacadeUtils.getFacadeData(level, blockPos);
            if (data != null) {
                BlockPos relativePos = blockPos.subtract(position);
                facadeMap.put(relativePos, data);
            }
        });
    }

    @Inject(
            method = "save",
            at = @At("RETURN")
    )
    private void onSave(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!facadeMap.isEmpty()) {
            ListTag facadesTag = new ListTag();

            facadeMap.forEach((pos, data) -> {
                CompoundTag facadeTag = new CompoundTag();
                facadeTag.putLong("pos", pos.asLong());

                if (data.isFullBlock()) {
                    facadeTag.putString("facade_type", "full");
                    facadeTag.put("state", NbtUtils.writeBlockState(data.getFullBlock()));
                } else if (data.isDirectional()) {
                    facadeTag.putString("facade_type", "directional");
                    CompoundTag facesTag = new CompoundTag();
                    data.directional().forEach((dir, state) ->
                            facesTag.put(dir.getSerializedName(), NbtUtils.writeBlockState(state)));
                    facadeTag.put("faces", facesTag);
                }

                facadesTag.add(facadeTag);
            });

            tag.put(FACADES_TAG, facadesTag);
        }
    }

    @Inject(
            method = "load",
            at = @At("RETURN")
    )
    private void onLoad(net.minecraft.core.HolderGetter<net.minecraft.world.level.block.Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        facadeMap.clear();

        if (tag.contains(FACADES_TAG)) {
            ListTag facadesTag = tag.getList(FACADES_TAG).get();

            for (int i = 0; i < facadesTag.size(); i++) {
                Optional<CompoundTag> _facadeTag = facadesTag.getCompound(i);
                if (_facadeTag.isPresent()) {
                    CompoundTag facadeTag = _facadeTag.get();
                    Optional<BlockPos> posOpt = facadeTag.getLong("pos").map(BlockPos::of);
                    if (posOpt.isEmpty()) continue;

                    BlockPos pos = posOpt.get();
                    Optional<String> facadeType = facadeTag.getString("facade_type");

                    if (facadeType.isPresent() && "directional".equals(facadeType.get())) {
                        Optional<CompoundTag> _facesTag = facadeTag.getCompound("faces");
                        if (_facesTag.isPresent()) {
                            CompoundTag facesTag = _facesTag.get();
                            EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
                            for (Direction dir : Direction.values()) {
                                String key = dir.getSerializedName();
                                if (facesTag.contains(key)) {
                                    BlockState state = NbtUtils.readBlockState(blockGetter, facesTag.getCompound(key).get());
                                    faces.put(dir, state);
                                }
                            }
                            if (!faces.isEmpty()) {
                                facadeMap.put(pos, FacadeData.directional(faces));
                            }
                        }
                    } else {
                        BlockState state = NbtUtils.readBlockState(blockGetter, facadeTag.getCompound("state").get());
                        facadeMap.put(pos, FacadeData.fullBlock(state));
                    }
                }
            }
        }
    }

    @Inject(
            method = "placeInWorld",
            at = @At("RETURN")
    )
    private void onPlaceInWorld(ServerLevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (!facadeMap.isEmpty() && cir.getReturnValue()) {
            facadeMap.forEach((relativePos, facadeData) -> {
                BlockPos transformedPos = StructureTemplate.calculateRelativePosition(settings, relativePos);
                BlockPos actualPos = transformedPos.offset(offset);

                if (facadeData.isFullBlock()) {
                    BlockState transformedState = facadeData.getFullBlock()
                            .mirror(settings.getMirror())
                            .rotate(settings.getRotation());
                    FacadeUtils.addFacade(level.getLevel(), actualPos, transformedState);
                } else if (facadeData.isDirectional()) {
                    facadeData.directional().forEach((dir, state) -> {
                        BlockState transformedState = state
                                .mirror(settings.getMirror())
                                .rotate(settings.getRotation());
                        FacadeUtils.addDirectionalFacade(level.getLevel(), actualPos, dir, transformedState);
                    });
                }
            });
        }
    }
}
