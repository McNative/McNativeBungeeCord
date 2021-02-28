/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.05.20, 09:31
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

package org.mcnative.runtime.bungeecord.event.server;

import net.md_5.bungee.api.event.ServerConnectEvent;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ServerConnectReason;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.proxy.event.player.MinecraftPlayerServerConnectEvent;

public class BungeeServerConnectEvent implements MinecraftPlayerServerConnectEvent {

    private final ServerConnectEvent original;
    private final BungeeCordServerMap serverMap;
    private final OnlineMinecraftPlayer player;
    private final ServerConnectReason reason;

    public BungeeServerConnectEvent(ServerConnectEvent original, BungeeCordServerMap serverMap, OnlineMinecraftPlayer player) {
        this.original = original;
        this.serverMap = serverMap;
        this.player = player;

        this.reason = mapReason();
    }

    @Override
    public MinecraftServer getTarget() {
        return serverMap.getMappedServer(original.getTarget());
    }

    @Override
    public void setTarget(MinecraftServer server) {
        original.setTarget(serverMap.getMappedInfo(server));
    }

    @Override
    public ServerConnectReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return original.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.original.setCancelled(cancelled);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }

    private ServerConnectReason mapReason(){
        switch (original.getReason()) {
            case JOIN_PROXY: return ServerConnectReason.LOGIN;
            case PLUGIN:
            case PLUGIN_MESSAGE:
            case COMMAND: return ServerConnectReason.API;
            case KICK_REDIRECT: return ServerConnectReason.REDIRECT_KICK;
            case SERVER_DOWN_REDIRECT: return ServerConnectReason.REDIRECT_SERVER_DOWN;
            case LOBBY_FALLBACK: return ServerConnectReason.FALLBACK;
            default: return ServerConnectReason.UNKNOWN;
        }
    }
}
