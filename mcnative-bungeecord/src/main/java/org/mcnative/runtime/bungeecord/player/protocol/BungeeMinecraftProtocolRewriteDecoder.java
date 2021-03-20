package org.mcnative.runtime.bungeecord.player.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.mcnative.runtime.api.connection.MinecraftConnection;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.protocol.Endpoint;
import org.mcnative.runtime.api.protocol.packet.PacketDirection;
import org.mcnative.runtime.api.protocol.packet.PacketManager;
import org.mcnative.runtime.bungeecord.player.protocol.positioning.PositionExtractor;
import org.mcnative.runtime.bungeecord.player.protocol.positioning.PositionPacketVersionId;
import org.mcnative.runtime.protocol.java.netty.rewrite.MinecraftProtocolRewriteDecoder;

import java.util.List;
import java.util.Map;

public class BungeeMinecraftProtocolRewriteDecoder extends MinecraftProtocolRewriteDecoder {

    private final PendingConnection connection;
    private final PositionPacketVersionId versionId;

    public BungeeMinecraftProtocolRewriteDecoder(PacketManager packetManager, Endpoint endpoint, PacketDirection direction, PendingConnection connection) {
        super(packetManager, endpoint, direction, connection);
        this.connection = connection;
        this.versionId = PositionPacketVersionId.get(connection.getProtocolVersion());
    }

    @Override
    public void handleInternalPacketManipulation(int packetId, ByteBuf buffer) {
        if(versionId.getPosition() == packetId){
            buffer.markReaderIndex();
            PositionExtractor.extractPosition(connection,buffer);
            buffer.resetReaderIndex();
        }else if(versionId.getRotation() == packetId){
            buffer.markReaderIndex();
            PositionExtractor.extractRotation(connection,buffer);
            buffer.resetReaderIndex();
        }else if(versionId.getCombined() == packetId){
            buffer.markReaderIndex();
            PositionExtractor.extractCombinedPosition(connection,buffer);
            buffer.resetReaderIndex();
        }
    }
}
