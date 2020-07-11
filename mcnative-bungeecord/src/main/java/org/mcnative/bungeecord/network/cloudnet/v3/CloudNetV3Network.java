/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 06.04.20, 09:55
 * @web %web%
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

package org.mcnative.bungeecord.network.cloudnet.v3;

import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.wrapper.Wrapper;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.synchronisation.NetworkSynchronisationCallback;
import net.pretronic.libraries.utility.exception.OperationFailedException;
import org.mcnative.common.network.Network;
import org.mcnative.common.network.NetworkIdentifier;
import org.mcnative.common.network.NetworkOperations;
import org.mcnative.common.network.component.server.MinecraftServer;
import org.mcnative.common.network.component.server.ProxyServer;
import org.mcnative.common.network.messaging.Messenger;
import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.protocol.packet.MinecraftPacket;
import org.mcnative.common.text.components.MessageComponent;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executor;

public class CloudNetV3Network implements Network {

    private final CloudNetV3Messenger messenger;
    private final NetworkIdentifier localIdentifier;

    public CloudNetV3Network(Executor executor) {
        this.messenger = new CloudNetV3Messenger(executor);
        this.localIdentifier = new NetworkIdentifier(
                Wrapper.getInstance().getServiceId().getName()
                ,Wrapper.getInstance().getServiceId().getUniqueId());
    }

    @Override
    public String getTechnology() {
        return "CloudNet V3";
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public NetworkOperations getOperations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConnected() {
        return messenger.isAvailable();
    }

    @Override
    public EventBus getEventBus() {
        throw new UnsupportedOperationException("Network event bus is currently not integrated");
    }

    @Override
    public CommandManager getCommandManager() {
        throw new UnsupportedOperationException("Network event bus is currently not integrated");
    }

    @Override
    public NetworkIdentifier getLocalIdentifier() {
        return localIdentifier;
    }


    @Override
    public NetworkIdentifier getIdentifier(String name) {
        ServiceInfoSnapshot service = Wrapper.getInstance().getCloudServiceProvider().getCloudServiceByName(name);
        if(service == null) throw new OperationFailedException("Server is not registered in cloud");
        return new NetworkIdentifier(name,service.getServiceId().getUniqueId());
    }


    @Override
    public Collection<ProxyServer> getProxies() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public ProxyServer getProxy(String name) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public ProxyServer getProxy(UUID uniqueId) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public ProxyServer getProxy(InetSocketAddress address) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<MinecraftServer> getServers() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public MinecraftServer getServer(String name) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public MinecraftServer getServer(UUID uniqueId) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public MinecraftServer getServer(InetSocketAddress address) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void sendBroadcastMessage(String channel, Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST, channel,request);
    }

    @Override
    public void sendProxyMessage(String channel, Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST_PROXY, channel,request);
    }

    @Override
    public void sendServerMessage(String channel, Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST_SERVER, channel,request);
    }

    @Override
    public Collection<NetworkSynchronisationCallback> getStatusCallbacks() {
        return Collections.emptyList();
    }

    @Override
    public void registerStatusCallback(Plugin<?> owner, NetworkSynchronisationCallback synchronisationCallback) {

    }

    @Override
    public void unregisterStatusCallback(NetworkSynchronisationCallback synchronisationCallback) {

    }

    @Override
    public void unregisterStatusCallbacks(Plugin<?> owner) {

    }

    @Override
    public int getOnlineCount() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String nme) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void broadcastPacket(MinecraftPacket packet) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void broadcastPacket(MinecraftPacket packet, String permission) {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public void kickAll(MessageComponent<?> component, VariableSet variables) {
        throw new UnsupportedOperationException("Currently not supported");
    }
}
