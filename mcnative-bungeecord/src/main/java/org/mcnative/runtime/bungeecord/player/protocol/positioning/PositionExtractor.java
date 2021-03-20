package org.mcnative.runtime.bungeecord.player.protocol.positioning;

import io.netty.buffer.ByteBuf;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.utils.positioning.Position;
import org.mcnative.runtime.bungeecord.player.BungeeProxiedPlayer;

public class PositionExtractor {

    public static void extractPosition(PendingConnection connection, ByteBuf buffer) {
        Position position = ((BungeeProxiedPlayer)connection.getPlayer()).getDirectPosition();
        position.setX(buffer.readDouble());
        position.setY(buffer.readDouble());
        position.setZ(buffer.readDouble());
    }

    public static void extractRotation(PendingConnection connection, ByteBuf buffer) {
        Position position = ((BungeeProxiedPlayer)connection.getPlayer()).getDirectPosition();
        position.setYaw(buffer.readFloat());
        position.setPitch(buffer.readFloat());
    }

    public static void extractCombinedPosition(PendingConnection connection, ByteBuf buffer) {
        Position position = ((BungeeProxiedPlayer)connection.getPlayer()).getDirectPosition();
        position.setX(buffer.readDouble());
        position.setY(buffer.readDouble());
        position.setZ(buffer.readDouble());
        position.setYaw(buffer.readFloat());
        position.setPitch(buffer.readFloat());
    }

}
