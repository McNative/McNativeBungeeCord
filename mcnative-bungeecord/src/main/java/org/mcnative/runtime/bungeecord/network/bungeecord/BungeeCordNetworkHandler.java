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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import org.mcnative.runtime.bungeecord.McNativeLauncher;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.proxy.ProxyService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeCordNetworkHandler implements Listener {

    private final List<UUID> pendingPlayers;

    public BungeeCordNetworkHandler() {
        this.pendingPlayers = new ArrayList<>();
        ProxyServer.getInstance().getPluginManager().registerListener(McNativeLauncher.getPlugin(),this);
    }

    public void handleRequest(Server server, Document request){
        String action = request.getString("action");
        if(action.equalsIgnoreCase("initial-request")){
            sendInitialRequest(server);
        }
    }

    private void sendInitialRequest(Server receiver){
        Document data = Document.newDocument();
        Document servers = Document.factory().newArrayEntry("servers");
        data.addEntry(servers);
        data.set("action","initial-request");
        data.set("proxy", ProxyService.getInstance().getAddress());
        data.set("local",receiver.getInfo().getName());
        data.set("networkId", McNative.getInstance().getNetwork().getIdentifier().getUniqueId());
        for (MinecraftServer server : ProxyService.getInstance().getServers()) {
            Document serverData = Document.newDocument();
            servers.addEntry(serverData);
            serverData.set("name",server.getName());
            serverData.set("type",server.getType().name());
            serverData.set("address",server.getAddress());
            serverData.set("permission",server.getPermission());
            if(!server.getOnlinePlayers().isEmpty()){
                Document players =  Document.factory().newArrayEntry("players");
                serverData.addEntry(players);
                for (OnlineMinecraftPlayer player : server.getOnlinePlayers()) {
                    Document compiledPlayer = Document.newDocument();
                    compiledPlayer.set("uniqueId",player.getUniqueId());
                    compiledPlayer.set("name",player.getName());
                    compiledPlayer.set("address",player.getAddress());
                    compiledPlayer.set("onlineMode",player.isOnlineMode());
                    players.addEntry(compiledPlayer);
                }
            }
        }
        receiver.sendData(PluginMessageMessenger.CHANNEL_NAME_NETWORK,DocumentFileType.JSON.getWriter().write(data,StandardCharsets.UTF_8));
    }

    private void broadcastPlayerLogin(ProxiedPlayer player,Server server){
        Document data = Document.newDocument();
        data.set("uniqueId",player.getUniqueId());
        data.set("name",player.getName());
        data.set("address",player.getAddress());
        data.set("onlineMode",player.getPendingConnection().isOnlineMode());
        data.set("server",server.getInfo().getName());
        data.set("action","player-login");
        broadcast(data);
    }

    private void broadcastPlayerLogout(ProxiedPlayer player){
        Document data = Document.newDocument();
        data.set("uniqueId",player.getUniqueId());
        data.set("action","player-logout");
        broadcast(data);
    }

    private void broadcastServerSwitch(ProxiedPlayer player, ServerInfo serverInfo){
        Document data = Document.newDocument();
        data.set("target",serverInfo.getName());
        data.set("uniqueId",player.getUniqueId());
        data.set("action","player-server-switch");
        broadcast(data);
    }

    private void broadcast(Document document){
        byte[] data = DocumentFileType.JSON.getWriter().write(document, StandardCharsets.UTF_8);
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if(!server.getPlayers().isEmpty()){
                server.sendData(PluginMessageMessenger.CHANNEL_NAME_NETWORK,data);
            }
        }
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        this.pendingPlayers.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event){
        boolean pending = this.pendingPlayers.remove(event.getPlayer().getUniqueId());
        if(pending){
           broadcastPlayerLogin(event.getPlayer(),event.getServer());
        }else{
            broadcastServerSwitch(event.getPlayer(),event.getServer().getInfo());
        }
        if(event.getServer().getInfo().getPlayers().size() == 0){
            sendInitialRequest(event.getServer());
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event){
        this.pendingPlayers.remove(event.getPlayer().getUniqueId());
        broadcastPlayerLogout(event.getPlayer());
    }
}
