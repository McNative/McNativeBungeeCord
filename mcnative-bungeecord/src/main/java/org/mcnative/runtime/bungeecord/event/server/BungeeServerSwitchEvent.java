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

import org.mcnative.runtime.api.event.server.MinecraftPlayerServerSwitchEvent;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

public class BungeeServerSwitchEvent implements MinecraftPlayerServerSwitchEvent {

    private final OnlineMinecraftPlayer player;
    private final MinecraftServer from;

    public BungeeServerSwitchEvent(OnlineMinecraftPlayer player,MinecraftServer from) {
        this.player = player;
        this.from = from;
    }

    @Override
    public MinecraftServer getFrom() {
        return from;
    }

    @Override
    public MinecraftServer getTo() {
        return player.getServer();
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
