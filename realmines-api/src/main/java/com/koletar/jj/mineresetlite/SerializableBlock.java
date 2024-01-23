package com.koletar.jj.mineresetlite;

import org.bukkit.*;

public class SerializableBlock
{
    private String blockId;
    private byte data;

    public SerializableBlock(final String s) {
        final String[] split = s.split(":");
        if (split.length < 1) {
            throw new IllegalArgumentException("String form of SerializableBlock didn't have sufficient data");
        }
        try {
            Material type = Material.getMaterial(split[0]);
            this.data = (byte)((split.length > 1) ? Byte.parseByte(split[1]) : 0);
            this.blockId = type.name();
        }
        catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable to convert id to integer and data to byte");
        }
    }

    public String getBlockId() {
        return this.blockId;
    }

    public byte getData() {
        return this.data;
    }
    @Override
    public boolean equals(final Object o) {
        return o instanceof SerializableBlock && this.blockId.equals(((SerializableBlock)o).blockId) && this.data == ((SerializableBlock)o).data;
    }
}