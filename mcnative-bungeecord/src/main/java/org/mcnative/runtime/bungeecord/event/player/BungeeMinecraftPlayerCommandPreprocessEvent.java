/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.06.20, 13:00
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

package org.mcnative.runtime.bungeecord.event.player;

import net.md_5.bungee.api.event.ChatEvent;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.api.event.player.MinecraftPlayerCommandPreprocessEvent;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

public class BungeeMinecraftPlayerCommandPreprocessEvent implements MinecraftPlayerCommandPreprocessEvent {

    private final ChatEvent event;
    private final OnlineMinecraftPlayer player;

    public BungeeMinecraftPlayerCommandPreprocessEvent(ChatEvent event, OnlineMinecraftPlayer player) {
        this.event = event;
        this.player = player;
    }

    @Override
    public String getCommand() {
        return event.getMessage();
    }

    @Override
    public void setCommand(String command) {
        Validate.notNull(command);
        event.setMessage(command);
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
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
