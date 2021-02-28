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

package org.mcnative.runtime.bungeecord.event.player;

import net.md_5.bungee.api.event.TabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

import java.util.List;

public class BungeeTabCompleteEvent implements MinecraftPlayerTabCompleteEvent {

    private final TabCompleteEvent original;
    private final OnlineMinecraftPlayer player;

    public BungeeTabCompleteEvent(TabCompleteEvent original, OnlineMinecraftPlayer player) {
        this.original = original;
        this.player = player;
    }

    @Override
    public String getCursor() {
        return original.getCursor();
    }

    @Override
    public List<String> getSuggestions() {
        return original.getSuggestions();
    }

    @Override
    public boolean isCancelled() {
        return original.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        original.setCancelled(cancelled);
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }
}
