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

import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.proxy.event.player.MinecraftPlayerServerKickEvent;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class BungeeServerKickEvent implements MinecraftPlayerServerKickEvent {

    private final BungeeCordServerMap serverMap;
    private final ServerKickEvent original;
    private final ConnectedMinecraftPlayer player;

    public BungeeServerKickEvent(BungeeCordServerMap serverMap, ServerKickEvent original, ConnectedMinecraftPlayer player) {
        this.serverMap = serverMap;
        this.original = original;
        this.player = player;
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return player.getServer();
    }

    @Override
    public MessageComponent<?> getKickReason() {
        return Text.decompile(ComponentSerializer.toString(original.getKickReasonComponent()));
    }

    @Override
    public MinecraftServer getFallbackServer() {
        return serverMap.getMappedServer(original.getCancelServer());
    }

    @Override
    public void setFallbackServer(MinecraftServer server) {
        if(server == null){
            original.setCancelServer(null);
            original.setCancelled(false);
        }else{
            original.setCancelServer(serverMap.getMappedInfo(server));
            original.setCancelled(true);
        }
    }

    @Override
    public void setKickReason(MessageComponent<?> cancelReason, VariableSet variables) {
        original.setKickReasonComponent(ComponentSerializer.parse(cancelReason.compileToString(player.getProtocolVersion(),variables)));
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }
}
