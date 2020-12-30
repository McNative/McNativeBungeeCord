/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.03.20, 15:44
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

package org.mcnative.runtime.bungeecord.player.permission;

import org.mcnative.runtime.bungeecord.player.BungeeProxiedPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;

import java.util.Collection;

public class BungeeCordPermissionProvider implements PermissionProvider {

    @Override
    public Collection<MinecraftPlayer> getOperators() {
        throw new UnsupportedOperationException("BungeeCord does not support operators");
    }

    @Override
    public Collection<String> getGroups() {
        throw new UnsupportedOperationException("BungeeCord does not support listing permission groups");
    }

    @Override
    public PermissionHandler getPlayerHandler(MinecraftPlayer player) {
        if(player instanceof BungeeProxiedPlayer){
            return new BungeeCordPermissionHandler((BungeeProxiedPlayer) player);
        }
        return null;
    }

    @Override
    public boolean createGroup(String name) {
        throw new UnsupportedOperationException("BungeeCord permission groups are only configure able");
    }

    @Override
    public boolean deleteGroup(String name) {
        throw new UnsupportedOperationException("BungeeCord permission groups are only configure able");
    }

    @Override
    public void setGroupPermission(String group, String permission, boolean allowed) {
        throw new UnsupportedOperationException("BungeeCord permission groups are only configure able");
    }

    @Override
    public void unsetGroupPermission(String group, String permission) {
        throw new UnsupportedOperationException("BungeeCord permission groups are only configure able");
    }
}
