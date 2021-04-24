/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 22.08.19, 18:57
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.runtime.bungeecord.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.pretronic.libraries.concurrent.Task;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.runtime.api.connection.MinecraftOutputStream;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.api.protocol.packet.type.sound.MinecraftSoundEffectPacket;
import org.mcnative.runtime.api.protocol.packet.type.sound.MinecraftStopSoundPacket;
import org.mcnative.runtime.api.utils.positioning.Position;
import org.mcnative.runtime.bungeecord.McNativeBungeeCordConfiguration;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.bungeecord.server.WrappedBungeeMinecraftServer;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.connection.ConnectionState;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ServerConnectReason;
import org.mcnative.runtime.api.network.component.server.ServerConnectResult;
import org.mcnative.runtime.api.player.*;
import org.mcnative.runtime.api.player.bossbar.BossBar;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.chat.ChatPosition;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.scoreboard.BelowNameInfo;
import org.mcnative.runtime.api.player.scoreboard.sidebar.Sidebar;
import org.mcnative.runtime.api.player.sound.SoundCategory;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.protocol.Endpoint;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacket;
import org.mcnative.runtime.api.protocol.packet.PacketDirection;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftChatPacket;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftResourcePackSendPacket;
import org.mcnative.runtime.api.protocol.packet.type.MinecraftTitlePacket;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.common.player.DefaultBossBar;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.common.player.tablist.AbstractTablist;
import org.mcnative.runtime.common.utils.positioning.DefaultPosition;
import org.mcnative.runtime.protocol.java.netty.rewrite.MinecraftProtocolRewriteDecoder;
import org.mcnative.runtime.protocol.java.netty.rewrite.MinecraftProtocolRewriteEncoder;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/*
@Todo In proxy player
    - Below Name
    - Sidebar
    - Tablist => Fehler mit fake player
 */
public class BungeeProxiedPlayer extends OfflineMinecraftPlayer implements ConnectedMinecraftPlayer {

    private final BungeeCordServerMap serverMap;
    private final BungeePendingConnection connection;
    private final Position position;

    private net.md_5.bungee.api.connection.ProxiedPlayer original;

    private MinecraftServer server;
    private PlayerClientSettings settings;

    private ChatChannel chatChannel;

    private Tablist tablist;
    private final Collection<BossBar> bossBars;
    private final Map<TablistEntry,String> tablistTeamNames;
    private int tablistTeamIndex;

    private boolean firstJoin;

    public BungeeProxiedPlayer(BungeeCordServerMap serverMap,BungeePendingConnection connection, MinecraftPlayerData playerData) {
        super(playerData);
        this.serverMap = serverMap;
        this.connection = connection;
        this.firstJoin = true;

        this.tablistTeamNames = new HashMap<>();
        this.tablistTeamIndex = 0;
        this.position = new DefaultPosition();
        this.bossBars = new ArrayList<>();
    }

    @Internal
    public net.md_5.bungee.api.connection.ProxiedPlayer getOriginal() {
        return original;
    }

    public BungeePendingConnection getPendingConnection(){
        return connection;
    }

    @Override
    public String getName() {
        return connection.getName();
    }

    @Override
    public void sendMessage(String message) {
        sendMessage(Text.of(message));
    }

    @Override
    public UUID getUniqueId() {
        return connection.getUniqueId();
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public void connect(MinecraftServer target) {
        if(original == null) throw new IllegalArgumentException("Player is not finally connected.");
        original.connect(serverMap.getMappedInfo(target));
    }

    @Override
    public void connect(MinecraftServer target, ServerConnectReason reason) {
        if(original == null) throw new IllegalArgumentException("Player is not finally connected.");
        original.connect(serverMap.getMappedInfo(target),mapReason(reason));
    }

    @Override
    public CompletableFuture<ServerConnectResult> connectAsync(MinecraftServer target,ServerConnectReason reason) {
        if(original == null) throw new IllegalArgumentException("Player is not finally connected.");
        CompletableFuture<ServerConnectResult> future = new CompletableFuture<>();
        ServerConnectRequest.Builder requestBuilder = ServerConnectRequest.builder()
                .retry(false)
                .reason(mapReason(reason))
                .target(serverMap.getMappedInfo(target))
                .callback((result, error) -> {
                    if(error != null) future.completeExceptionally(error);
                    else future.complete(mapResult(result));
                });
        original.connect(requestBuilder.build());
        return future;
    }

    private ServerConnectEvent.Reason mapReason(ServerConnectReason reason){
        switch (reason){
            case LOGIN: return ServerConnectEvent.Reason.JOIN_PROXY;
            case API: return ServerConnectEvent.Reason.PLUGIN;
            case FALLBACK: return ServerConnectEvent.Reason.LOBBY_FALLBACK;
            case REDIRECT_KICK: return ServerConnectEvent.Reason.KICK_REDIRECT;
            case REDIRECT_SERVER_DOWN: return ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT;
            default:return ServerConnectEvent.Reason.UNKNOWN;
        }
    }

    private ServerConnectResult mapResult(ServerConnectRequest.Result result){
        switch (result){
            case SUCCESS: return ServerConnectResult.SUCCESS;
            case ALREADY_CONNECTED: return ServerConnectResult.ALREADY_CONNECTED;
            case ALREADY_CONNECTING: return ServerConnectResult.ALREADY_CONNECTING;
            case EVENT_CANCEL: return ServerConnectResult.CANCEL;
            default: return ServerConnectResult.FAIL;
        }
    }

    @Override
    public ChatChannel getPrimaryChatChannel() {
        return chatChannel;
    }

    @Override
    public void setPrimaryChatChannel(ChatChannel channel) {
        if(!channel.containsPlayer(this)) channel.addPlayer(this);
        this.chatChannel = channel;
    }

    @Override
    public void kick(MessageComponent<?> message, VariableSet variables) {
        connection.disconnect(message,variables);
    }

    @Override
    public DeviceInfo getDevice() {
        return DeviceInfo.JAVA;
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return connection.getProtocolVersion();
    }

    @Override
    public ConnectionState getState() {
        return connection.getState();
    }

    @Override
    public boolean isOnlineMode() {
        return connection.isOnlineMode();
    }

    @Override
    public PlayerClientSettings getClientSettings() {
        return settings;
    }

    @Override
    public int getPing() {
        if(original == null) throw new IllegalArgumentException("Player is not finally connected.");
        return original.getPing();
    }

    @Override
    public CompletableFuture<Integer> getPingAsync() {
        if(original == null) throw new IllegalArgumentException("Player is not finally connected.");
        CompletableFuture<Integer> result = new CompletableFuture<>();
        McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                .async().execute(() -> result.complete(original.getPing()));
        return result;
    }

    @Override
    public org.mcnative.runtime.api.network.component.server.ProxyServer getProxy() {
        return (org.mcnative.runtime.api.network.component.server.ProxyServer) McNative.getInstance().getLocal();
    }

    @Override
    public Sidebar getSidebar() {
        throw new UnsupportedOperationException("Coming soon");
    }

    @Override
    public void setSidebar(Sidebar sidebar) {
        throw new UnsupportedOperationException("Coming soon");
    }

    @Override
    public Tablist getTablist() {
        return tablist;
    }

    @Override
    public void setTablist(Tablist tablist) {
        if(this.tablist != null){
            ((AbstractTablist)this.tablist).detachReceiver(this);
        }
        this.tablistTeamNames.clear();
        if(tablist != null){
            this.tablist = tablist;
            ((AbstractTablist)this.tablist).attachReceiver(this);
        }
    }

    @Override
    public BelowNameInfo getBelowNameInfo() {
        throw new UnsupportedOperationException("Coming soon");
    }

    @Override
    public void setBelowNameInfo(BelowNameInfo info) {
        throw new UnsupportedOperationException("Coming soon");
    }

    @Override
    public void performCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(original,command);
    }

    @Override
    public void chat(String message) {
        original.chat(message);
    }

    @Override
    public void sendMessage(ChatPosition position, MessageComponent<?> message, VariableSet variables) {
        MinecraftChatPacket packet = new MinecraftChatPacket();
        packet.setPosition(position);
        packet.setMessage(message);
        packet.setVariables(variables);
        sendPacket(packet);
    }

    @Override
    public void sendTitle(Title title) {
        MinecraftTitlePacket timePacket = new MinecraftTitlePacket();
        timePacket.setAction(MinecraftTitlePacket.Action.SET_TIME);
        timePacket.setTime(title.getTiming());
        sendPacket(timePacket);

        if(title.getTitle() != null){
            MinecraftTitlePacket titlePacket = new MinecraftTitlePacket();
            titlePacket.setAction(MinecraftTitlePacket.Action.SET_TITLE);
            titlePacket.setMessage(title.getTitle());
            titlePacket.setVariables(title.getVariables());
            sendPacket(titlePacket);
        }

        if(title.getSubTitle() != null){
            MinecraftTitlePacket subTitle = new MinecraftTitlePacket();
            subTitle.setAction(MinecraftTitlePacket.Action.SET_SUBTITLE);
            subTitle.setMessage(title.getSubTitle());
            subTitle.setVariables(title.getVariables());
            sendPacket(subTitle);
        }
    }

    @Override
    public void resetTitle() {
        MinecraftTitlePacket packet = new MinecraftTitlePacket();
        packet.setAction(MinecraftTitlePacket.Action.RESET);
        sendPacket(packet);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables) {
        MinecraftChatPacket packet = new MinecraftChatPacket();
        packet.setPosition(ChatPosition.ACTIONBAR);
        packet.setMessage(message);
        packet.setVariables(variables);
        sendPacket(packet);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables, long staySeconds) {
        sendActionbar(message, variables);

        if(staySeconds > 0){
            long timeout = System.currentTimeMillis()+TimeUnit.SECONDS.toMillis(staySeconds);
            final Task task = McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM)
                    .async().interval(3,TimeUnit.SECONDS).delay(1,TimeUnit.SECONDS).create();
            task.append(() -> {
                if(System.currentTimeMillis() > timeout){
                    task.destroy();
                }else{
                    sendActionbar(message, variables);
                }
            });
        }
    }

    @Override
    public Collection<BossBar> getActiveBossBars() {
        return this.bossBars;
    }

    @Override
    public void addBossBar(BossBar bossBar) {
        if(!this.bossBars.contains(bossBar)){
            this.bossBars.add(bossBar);
            ((DefaultBossBar)bossBar).attachReceiver(this);
        }
    }

    @Override
    public void removeBossBar(BossBar bossBar) {
        if(this.bossBars.contains(bossBar)){
            this.bossBars.remove(bossBar);
            ((DefaultBossBar)bossBar).detachReceiver(this);
        }
    }

    public void clearBossBar(){
        for (BossBar bossBar : this.bossBars) {
            ((DefaultBossBar)bossBar).detachReceiver(this);
        }
        this.bossBars.clear();
    }


    @Override
    public void sendResourcePackRequest(String url) {
        sendResourcePackRequest(url,"");
    }

    @Override
    public void sendResourcePackRequest(String url, String hash) {
        MinecraftResourcePackSendPacket packet = new MinecraftResourcePackSendPacket();
        packet.setUrl(url);
        packet.setHash(hash);
        sendPacket(packet);
    }

    @Override
    public PendingConnection getConnection() {
        return connection;
    }

    @Override
    public InetSocketAddress getAddress() {
        return connection.getAddress();
    }

    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }

    @Override
    public void disconnect(MessageComponent<?> reason, VariableSet variables) {
        connection.disconnect(reason, variables);
    }

    @Override
    public void sendPacket(MinecraftPacket packet) {
        connection.sendPacket(packet);
    }

    @Override
    public void playSound(String sound, SoundCategory category, float volume, float pitch) {
        MinecraftSoundEffectPacket packet = new MinecraftSoundEffectPacket();
        packet.setSoundName(sound);
        packet.setCategory(category);
        packet.setVolume(volume);
        packet.setPitch(pitch);
        packet.setPositionX(position.getBlockX());
        packet.setPositionY(position.getBlockY());
        packet.setPositionZ(position.getBlockZ());
        sendPacket(packet);
    }

    @Override
    public void stopSound() {
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setAction(MinecraftStopSoundPacket.Action.ALL);
        sendPacket(packet);
    }

    @Override
    public void stopSound(String sound) {
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setSoundName(sound);
        packet.setAction(MinecraftStopSoundPacket.Action.SOUND);
        sendPacket(packet);
    }

    @Override
    public void stopSound(SoundCategory category) {
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setCategory(category);
        packet.setAction(MinecraftStopSoundPacket.Action.CATEGORY);
        sendPacket(packet);
    }

    @Override
    public void stopSound(String sound, SoundCategory category) {
        MinecraftStopSoundPacket packet = new MinecraftStopSoundPacket();
        packet.setCategory(category);
        packet.setSoundName(sound);
        packet.setAction(MinecraftStopSoundPacket.Action.BOTH);
        sendPacket(packet);
    }

    @Override
    public void sendLocalLoopPacket(MinecraftPacket packet) {
        connection.sendLocalLoopPacket(packet);
    }

    @Override
    public void sendRawPacket(ByteBuf byteBuf) {
        connection.sendRawPacket(byteBuf);
    }

    @Override
    public OutputStream sendData(String channel) {
        return new MinecraftOutputStream(channel,this);
    }

    @Override
    public void sendData(String channel, byte[] output) {
        original.sendData(channel,output);
    }

    @Override
    public <T> T getAs(Class<T> otherPlayerClass) {
        return McNative.getInstance().getPlayerManager().translate(otherPlayerClass,this);
    }

    @Override
    public OnlineMinecraftPlayer getAsOnlinePlayer() {
        return this;
    }

    @Override
    public boolean isOnline() {
        return isConnected();
    }

    @Override
    public boolean equals(Object object) {
        if(object == this || (original != null && original.equals(object))) return true;
        else if(object instanceof MinecraftPlayerComparable) return ((MinecraftPlayerComparable) object).equals(this);
        else if(object instanceof MinecraftPlayer) return ((MinecraftPlayer) object).getUniqueId().equals(getUniqueId());
        return false;
    }

    @Internal
    public void postLogin(net.md_5.bungee.api.connection.ProxiedPlayer original){
        this.original = original;
        connection.setState(ConnectionState.GAME);

        ChatChannel serverChat = McNative.getInstance().getLocal().getServerChat();
        if(serverChat != null){
            serverChat.addPlayer(this);
            this.chatChannel = serverChat;
        }
    }

    @Internal
    public void setSettings(PlayerClientSettings settings){
        this.settings = settings;
    }

    @Internal
    public void setServer(MinecraftServer server){
        if(this.server instanceof WrappedBungeeMinecraftServer){
            ((WrappedBungeeMinecraftServer) server).removePlayer(this);
        }
        this.server = server;
        if(server instanceof WrappedBungeeMinecraftServer){
            ((WrappedBungeeMinecraftServer) server).addPlayer(this);
        }
    }

    @Internal
    public void injectDownstreamProtocolHandlersToPipeline(){
        if(!McNativeBungeeCordConfiguration.NETWORK_PACKET_MANIPULATION_DOWNSTREAM_ENABLED) return;
        Object connection = ReflectionUtil.getFieldValue(original,"server");
        if(connection == null) return;

        Object channelWrapper = ReflectionUtil.getFieldValue(connection,"ch");
        Channel channel = ReflectionUtil.getFieldValue(channelWrapper,"ch",Channel.class);

        channel.pipeline().addAfter("packet-encoder","mcnative-packet-rewrite-encoder"
                ,new MinecraftProtocolRewriteEncoder(McNative.getInstance().getLocal().getPacketManager()
                        ,Endpoint.DOWNSTREAM, PacketDirection.OUTGOING,this.connection));

         channel.pipeline().addBefore("packet-decoder","mcnative-packet-rewrite-decoder"
                ,new MinecraftProtocolRewriteDecoder(McNative.getInstance().getLocal().getPacketManager()
                        ,Endpoint.DOWNSTREAM, PacketDirection.INCOMING,this));
    }

    @Internal
    public void handleLogout(){
        if(this.permissionHandler != null) this.permissionHandler.onPlayerLogout();
        ChatChannel serverChat = McNative.getInstance().getLocal().getServerChat();
        if(serverChat != null) serverChat.removePlayer(this);

        if(this.server instanceof WrappedBungeeMinecraftServer){
            ((WrappedBungeeMinecraftServer) server).removePlayer(this);
        }
    }

    @Internal
    public boolean isFirstJoin() {
        return firstJoin;
    }

    @Internal
    public void setFirstJoin(boolean firstJoin) {
        this.firstJoin = firstJoin;
    }

    @Internal
    public Map<TablistEntry, String> getTablistTeamNames() {
        return tablistTeamNames;
    }

    @Internal
    public int getTablistTeamIndexAndIncrement(){
        return tablistTeamIndex++;
    }

    @Override
    public Position getPosition() {
        return position.clone();
    }

    @Internal
    public Position getDirectPosition() {
        return position;
    }
}

