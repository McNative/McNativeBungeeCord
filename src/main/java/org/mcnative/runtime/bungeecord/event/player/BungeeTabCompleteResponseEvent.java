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

import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;

import java.util.List;

public class BungeeTabCompleteResponseEvent implements MinecraftPlayerTabCompleteResponseEvent {

    private final TabCompleteResponseEvent original;
    private final OnlineMinecraftPlayer player;
    private final String cursor;

    public BungeeTabCompleteResponseEvent(TabCompleteResponseEvent original, OnlineMinecraftPlayer player, String cursor) {
        this.original = original;
        this.player = player;
        this.cursor = cursor;
    }

    @Override
    public String getCursor() {
        return cursor;
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
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }
}
