package com.portingdeadmods.cable_facades.multipart;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;

public final class CoverShapes {
    private static final double PX = 1.0 / 16.0;

    private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.DOWN, Shapes.box(0, 0, 0, 1, PX, 1));
        SHAPES.put(Direction.UP, Shapes.box(0, 1 - PX, 0, 1, 1, 1));
        SHAPES.put(Direction.NORTH, Shapes.box(0, 0, 0, 1, 1, PX));
        SHAPES.put(Direction.SOUTH, Shapes.box(0, 0, 1 - PX, 1, 1, 1));
        SHAPES.put(Direction.WEST, Shapes.box(0, 0, 0, PX, 1, 1));
        SHAPES.put(Direction.EAST, Shapes.box(1 - PX, 0, 0, 1, 1, 1));
    }

    private CoverShapes() {}

    public static VoxelShape get(Direction dir) {
        return SHAPES.get(dir);
    }
}
