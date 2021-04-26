/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.08.19, 18:19
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

package org.mcnative.runtime.bungeecord.server;

import net.md_5.bungee.api.config.ServerInfo;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.message.bml.variable.describer.VariableObjectToString;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.exception.OperationFailedException;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.MinecraftServerType;
import org.mcnative.runtime.api.network.component.server.ServerStatusRequester;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.network.messaging.Messenger;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.protocol.packet.MinecraftPacket;
import org.mcnative.runtime.api.text.components.MessageComponent;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WrappedBungeeMinecraftServer implements MinecraftServer, VariableObjectToString {

    private final ServerInfo original;

    private String permission;
    private MinecraftServerType type;

    private final Collection<OnlineMinecraftPlayer> players;
    private NetworkIdentifier identifier;

    public WrappedBungeeMinecraftServer(ServerInfo info) {
        this.original = info;
        this.permission = info.isRestricted() ? info.getPermission() : null;
        this.type = MinecraftServerType.NORMAL;
        this.players = new ArrayList<>();
    }

    public ServerInfo getOriginalInfo(){
        return original;
    }

    @Override
    public String getName() {
        return original.getName();
    }

    @Override
    public String getGroup() {
        return getIdentifier().getGroup();
    }

    @Override
    public String getStatus() {
        return null;
    }

    @Override
    public int getMaxPlayerCount() {
        return ping().getMaxPlayers();
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public MinecraftServerType getType() {
        return this.type;
    }

    @Override
    public void setType(MinecraftServerType type) {
        this.type = type;
    }

    @Override
    public EventBus getEventBus() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CommandManager getCommandManager() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean isOnline() {
        try{
            return !original.getPlayers().isEmpty() || ping() != null;
        }catch (Exception ignore){}
        return false;
    }

    @Override
    public ServerStatusResponse ping() {
        return ServerStatusRequester.getDefault().requestStatus(this);
    }

    @Override
    public CompletableFuture<ServerStatusResponse> pingAsync() {
        return ServerStatusRequester.getDefault().requestStatusAsync(this);
    }

    @Override
    public void sendData(String channel, Document document) {
        sendData(channel, DocumentFileType.JSON.getWriter().write(document, StandardCharsets.UTF_8));
    }

    @Override
    public void sendData(String channel, byte[] bytes) {
        original.sendData(channel, bytes,false);
    }

    @Override
    public void sendData(String channel, byte[] data,boolean queued) {
        original.sendData(channel, data,queued);
    }

    @Override
    public InetSocketAddress getAddress() {
        try{
            return (InetSocketAddress) original.getSocketAddress();
        }catch (NoSuchMethodError ignored) {
            return original.getAddress();
        }
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return ping().getVersion().getProtocol();
    }

    @Override
    public int getOnlineCount() {
        return original.getPlayers().size();
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        return players;
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        Validate.notNull(uniqueId);
        return Iterators.findOne(this.players, player -> player.getUniqueId().equals(uniqueId));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        Validate.notNull(name);
        return Iterators.findOne(this.players, player -> player.getName().equals(name));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        return Iterators.findOne(this.players, player -> player.getXBoxId() == xBoxId);
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(component,variables);
        getOnlinePlayers().forEach(player -> player.sendMessage(component,variables));
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(permission,component,variables);
        getOnlinePlayers().forEach(player -> {
            if(player.hasPermission(permission)){
                player.sendMessage(component, variables);
            }
        });
    }

    @Override
    public NetworkIdentifier getIdentifier() {
        if(this.identifier == null) this.identifier = McNative.getInstance().getNetwork().getIdentifier(original.getName());
        if(this.identifier == null) throw new OperationFailedException("Server not registered in network technology");
        return identifier;
    }

    @Override
    public void sendMessage(String channel, Document request) {
        McNative.getInstance().getRegistry().getService(Messenger.class).sendMessage(this,channel,request);
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String channel, Document request) {
        return McNative.getInstance().getRegistry().getService(Messenger.class).sendQueryMessageAsync(this,channel,request);
    }

    @Override
    public boolean equals(Object object) {
        if(object == this || original.equals(object)) return true;
        else if(object instanceof MinecraftServer){
            return ((MinecraftServer) object).getName().equalsIgnoreCase(original.getName())
                    && ((MinecraftServer) object).getAddress().equals(original.getAddress());
        }else if(object instanceof ServerInfo){
            return ((ServerInfo) object).getName().equalsIgnoreCase(((ServerInfo) object).getName())
                    && ((ServerInfo) object).getAddress().equals(original.getAddress());
        }
        return false;
    }

    @Override
    public String toStringVariable() {
        return getName();
    }

    @Internal
    public void addPlayer(OnlineMinecraftPlayer player){
        this.players.add(player);
    }

    @Internal
    public void removePlayer(OnlineMinecraftPlayer player){
        this.players.remove(player);
    }
}
