package com.irs.signals;

import net.minecraft.util.IStringSerializable;

public enum SignalAspect implements IStringSerializable {
    RED("red", 0xFF0000),
    YELLOW("yellow", 0xFFFF00),
    GREEN("green", 0x00FF00);

    private final String name;
    private final int color;

    SignalAspect(String name, int color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public SignalAspect next() {
        SignalAspect[] values = values();
        return values()[(ordinal() + 1) % values.length];
    }
}
