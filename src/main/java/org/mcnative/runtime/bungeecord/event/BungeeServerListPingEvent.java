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

package org.mcnative.runtime.bungeecord.event;

import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.ProxyPingEvent;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerStatusResponse;
import org.mcnative.runtime.api.event.service.local.LocalServicePingEvent;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;

import java.net.InetSocketAddress;

public class BungeeServerListPingEvent implements LocalServicePingEvent {

    private final PendingConnection connection;
    private final ProxyPingEvent event;
    private BungeeCordServerStatusResponse response;

    public BungeeServerListPingEvent(PendingConnection connection, ProxyPingEvent event) {
        this.connection = connection;
        this.event = event;
    }

    @Override
    public InetSocketAddress getClientAddress() {
        return connection.getAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return connection.getVirtualHost();
    }

    @Override
    public ServerStatusResponse getResponse() {
        if(response == null) response = new BungeeCordServerStatusResponse(event.getResponse());
        else response.setPing(event.getResponse());
        return response;
    }

    @Override
    public void setResponse(ServerStatusResponse response) {
        if(response instanceof BungeeCordServerStatusResponse){
            this.response = (BungeeCordServerStatusResponse) response;
            this.event.setResponse(((BungeeCordServerStatusResponse)response).getOriginal());
        }else{
            ServerStatusResponse original = getResponse();
            original.setFavicon(response.getFavicon());
            original.setMaxPlayers(response.getMaxPlayers());
            original.setOnlinePlayers(response.getOnlinePlayers());
            original.setDescription(response.getDescription());
            original.setPlayerInfo(response.getPlayerInfo());
            original.setVersion(response.getVersion());
        }
    }

}
