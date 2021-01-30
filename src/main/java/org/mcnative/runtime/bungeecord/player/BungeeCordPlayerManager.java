/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 20.09.19, 20:27
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

import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.annonations.Internal;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.common.player.AbstractPlayerManager;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeCordPlayerManager extends AbstractPlayerManager {

    private final Collection<ConnectedMinecraftPlayer> onlineMinecraftPlayers;

    public BungeeCordPlayerManager() {
        this.onlineMinecraftPlayers = ConcurrentHashMap.newKeySet();
    }

    public Collection<ConnectedMinecraftPlayer> getConnectedPlayers() {
        return onlineMinecraftPlayers;
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(UUID uniqueId) {
        return Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getUniqueId().equals(uniqueId));
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(long xBoxId) {
        return Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getXBoxId() == xBoxId);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(String name) {
        return Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getName().equalsIgnoreCase(name));
    }

    @Internal
    public ConnectedMinecraftPlayer getMappedPlayer(net.md_5.bungee.api.connection.ProxiedPlayer player0){
        ConnectedMinecraftPlayer result = Iterators.findOne(this.onlineMinecraftPlayers, player -> player.getUniqueId() == player0.getUniqueId());
        if(result == null) throw new IllegalArgumentException("McNative mapping error (BungeeCord -> McNative)");
        return result;
    }

    @Internal
    public void registerPlayer(ConnectedMinecraftPlayer player){
        this.onlineMinecraftPlayers.add(player);
    }

    @Internal
    public OnlineMinecraftPlayer unregisterPlayer(UUID uniqueId){
        return Iterators.removeOne(this.onlineMinecraftPlayers, player -> player.getUniqueId().equals(uniqueId));
    }
}