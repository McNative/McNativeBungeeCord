/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 29.12.19, 19:45
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

package org.mcnative.bungeecord.network;

import net.prematic.libraries.command.manager.CommandManager;
import net.prematic.libraries.document.Document;
import net.prematic.libraries.event.EventBus;
import net.prematic.libraries.message.bml.variable.VariableSet;
import org.mcnative.common.McNative;
import org.mcnative.common.network.Network;
import org.mcnative.common.network.NetworkIdentifier;
import org.mcnative.common.network.component.server.MinecraftServer;
import org.mcnative.common.network.component.server.ProxyServer;
import org.mcnative.common.network.messaging.MessagingProvider;
import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.protocol.packet.MinecraftPacket;
import org.mcnative.common.text.components.MessageComponent;
import org.mcnative.proxy.ProxyService;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BungeecordProxyNetwork implements Network {

    private final ProxyService service;

    public BungeecordProxyNetwork(ProxyService service) {
        this.service = service;
    }

    @Override
    public String getTechnology() {
        return "BungeeCord Proxy Network";
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public EventBus getEventBus() {
        throw new UnsupportedOperationException("Network events are currently not supported");
    }

    @Override
    public NetworkIdentifier getLocalIdentifier() {
        return NetworkIdentifier.BROADCAST_PROXY;//Only one proxy available
    }

    @Override
    public NetworkIdentifier getIdentifier(String name) {
        return new NetworkIdentifier(name,UUID.nameUUIDFromBytes(name.getBytes()));
    }

    @Override
    public CommandManager getCommandManager() {
        throw new UnsupportedOperationException("Network commands are currently not supported");
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public Collection<ProxyServer> getProxies() {
        return Collections.singleton(service);
    }

    @Override
    public ProxyServer getProxy(String name) {
        return service.getName().equalsIgnoreCase(name) ? service : null;
    }

    @Override
    public ProxyServer getProxy(UUID uniqueId) {
        return service.getIdentifier().getUniqueId().equals(uniqueId) ? service : null;
    }

    @Override
    public ProxyServer getProxy(InetSocketAddress address) {
        return service.getAddress().equals(address) ? service : null;
    }

    @Override
    public Collection<MinecraftServer> getServers() {
        return service.getServers();
    }

    @Override
    public MinecraftServer getServer(String name) {
        return service.getServer(name);
    }

    @Override
    public MinecraftServer getServer(UUID uniqueId) {
        return service.getServer(uniqueId);
    }

    @Override
    public MinecraftServer getServer(InetSocketAddress address) {
        return service.getServer(address);
    }

    @Override
    public void sendBroadcastMessage(String channel,Document request) {
        McNative.getInstance().getRegistry().getService(MessagingProvider.class)
                .sendMessage(NetworkIdentifier.BROADCAST,channel,request);
    }

    @Override
    public void sendProxyMessage(String channel,Document request) {
        McNative.getInstance().getRegistry().getService(MessagingProvider.class)
                .sendMessage(NetworkIdentifier.BROADCAST_PROXY,channel,request);
    }

    @Override
    public void sendServerMessage(String channel,Document request) {
        McNative.getInstance().getRegistry().getService(MessagingProvider.class)
                .sendMessage(NetworkIdentifier.BROADCAST_SERVER,channel,request);
    }

    @Override
    public int getOnlineCount() {
        return service.getOnlineCount();
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        return service.getOnlinePlayers();
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(int id) {
        return service.getOnlinePlayer(id);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        return service.getOnlinePlayer(uniqueId);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        return service.getOnlinePlayer(name);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        return service.getOnlinePlayer(xBoxId);
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        service.broadcast(component, variables);
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        service.broadcast(permission, component, variables);
    }

    @Override
    public void broadcastPacket(MinecraftPacket packet) {
        service.broadcastPacket(packet);
    }

    @Override
    public void broadcastPacket(MinecraftPacket packet, String permission) {
        service.broadcastPacket(packet,permission);
    }

    @Override
    public void kickAll(MessageComponent<?> component, VariableSet variables) {
        service.kickAll(component, variables);
    }

    @Override
    public void sendMessage(String channel, Document request) {

    }

    @Override
    public Document sendQueryMessage(String channel, Document request) {

        return null;
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String channel, Document request) {
        return null;
    }
}