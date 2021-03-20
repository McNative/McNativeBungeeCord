package org.mcnative.runtime.bungeecord.player.protocol.positioning;

import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;

public enum PositionPacketVersionId {

    V1_7(MinecraftProtocolVersion.JE_1_7,0x04,0x05,0x06),
    V1_9(MinecraftProtocolVersion.JE_1_9,0x0C,0x0E,0x0D),
    V1_11(MinecraftProtocolVersion.JE_1_11,0x0E,0x10,0x0F),
    V1_12(MinecraftProtocolVersion.JE_1_12,0x0D,0x0F,0x0E),
    V1_13(MinecraftProtocolVersion.JE_1_13,0x10,0x12,0x11),
    V1_14(MinecraftProtocolVersion.JE_1_14,0x11,0x13,0x12),
    V1_16(MinecraftProtocolVersion.JE_1_16,0x12,0x14,0x13);


    private final MinecraftProtocolVersion version;
    private final int position;
    private final int rotation;
    private final int combined;

    PositionPacketVersionId(MinecraftProtocolVersion version, int position, int rotation, int combined) {
        this.version = version;
        this.position = position;
        this.rotation = rotation;
        this.combined = combined;
    }

    public MinecraftProtocolVersion getVersion() {
        return version;
    }

    public int getPosition() {
        return position;
    }

    public int getRotation() {
        return rotation;
    }

    public int getCombined() {
        return combined;
    }

    public static PositionPacketVersionId get(MinecraftProtocolVersion version){
        PositionPacketVersionId result = V1_7;
        for (PositionPacketVersionId value : values()) {
            if(value.getVersion().isNewer(version)) break;
            result = value;
        }
        return result;
    }
}
