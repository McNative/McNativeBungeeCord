/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.05.20, 09:33
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

package org.mcnative.runtime.bungeecord.network.bungeecord;

import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.synchronisation.NetworkSynchronisationCallback;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.network.NetworkIdentifier;
import org.mcnative.runtime.api.network.NetworkOperations;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ProxyServer;
import org.mcnative.runtime.api.network.messaging.Messenger;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.proxy.ProxyService;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.common.network.event.NetworkEventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BungeecordProxyNetwork implements Network {

    private final ProxyService service;
    private final Messenger messenger;
    private final NetworkEventBus eventBus;

    private final UUID networkId;

    public BungeecordProxyNetwork(ProxyService service, ExecutorService executor, BungeeCordServerMap serverMap) {
        this.service = service;
        this.messenger = new PluginMessageMessenger(executor,serverMap);
        this.eventBus = new NetworkEventBus();
        this.messenger.registerChannel("mcnative_event",ObjectOwner.SYSTEM,eventBus);
        networkId = loadNetworkId();
    }

    private UUID loadNetworkId(){
        File file = new File("plugins/McNative/lib/runtime.dat");
        if(file.exists()){
            Document document = DocumentFileType.JSON.getReader().read(new File("plugins/McNative/lib/runtime.dat"));
            UUID uuid =  document.getObject("networkId",UUID.class);
            if(uuid != null) return uuid;
        }
        UUID uuid = UUID.randomUUID();
        Document document = Document.newDocument();
        document.set("networkId",uuid);
        file.getParentFile().mkdirs();
        DocumentFileType.JSON.getWriter().write(file,document,false);
        return uuid;
    }

    @Override
    public String getTechnology() {
        return "BungeeCord Proxy Network";
    }

    @Override
    public Messenger getMessenger() {
        return messenger;
    }

    @Override
    public NetworkOperations getOperations() {
        throw new UnsupportedOperationException("Are not required, use the normal proxy player");
    }

    @Override
    public boolean isConnected() {
        return true;//Always connected
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
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
    public NetworkIdentifier getIdentifier(UUID uuid) {
        if(service.getIdentifier().getUniqueId().equals(uuid)) return service.getIdentifier();
        for (MinecraftServer server : getServers()) {
            if(server.getIdentifier().getUniqueId().equals(uuid)) return server.getIdentifier();
        }
        return null;
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
    public Collection<NetworkSynchronisationCallback> getStatusCallbacks() {
        return Collections.emptyList(); //Unused always online
    }

    @Override
    public void registerStatusCallback(Plugin<?> owner, NetworkSynchronisationCallback synchronisationCallback) {
        //Unused always online
    }

    @Override
    public void unregisterStatusCallback(NetworkSynchronisationCallback synchronisationCallback) {
        //Unused always online
    }

    @Override
    public void unregisterStatusCallbacks(Plugin<?> owner) {
        //Unused always online
    }

    @Override
    public Collection<ProxyServer> getProxies() {
        return Collections.singleton(service);
    }

    @Override
    public Collection<ProxyServer> getProxies(String s) {
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
    public ProxyServer getLeaderProxy() {
        return ProxyService.getInstance();
    }

    @Override
    public boolean isLeaderProxy(ProxyServer server) {
        return server.getIdentifier().getUniqueId().equals(getLocalIdentifier().getUniqueId());
    }

    @Override
    public Collection<MinecraftServer> getServers() {
        return service.getServers();
    }

    @Override
    public Collection<MinecraftServer> getServers(String group) {
        Collection<MinecraftServer> result = new ArrayList<>();
        for (MinecraftServer server : getServers()) {
            if(server.getGroup().equalsIgnoreCase(group)){
                result.add(server);
            }
        }
        return result;
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
    public void sendBroadcastMessage(String channel,Document request) {
       messenger.sendMessage(NetworkIdentifier.BROADCAST,channel,request);
    }

    @Override
    public void sendProxyMessage(String channel,Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST_PROXY,channel,request);
    }

    @Override
    public void sendServerMessage(String channel,Document request) {
        messenger.sendMessage(NetworkIdentifier.BROADCAST_SERVER,channel,request);
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
        return McNative.getInstance().getLocal().getMaxPlayerCount();
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
    public NetworkIdentifier getIdentifier() {
        return new NetworkIdentifier(getName(),networkId);
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String s, Document document) {
        return null;
    }
}
