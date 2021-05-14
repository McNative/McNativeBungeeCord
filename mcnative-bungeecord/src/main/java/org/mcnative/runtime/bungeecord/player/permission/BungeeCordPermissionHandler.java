/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.03.20, 15:43
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
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionResult;

import java.util.Collection;

public class BungeeCordPermissionHandler implements PermissionHandler {

    private final BungeeProxiedPlayer player;
    private final PlayerDesign design;

    public BungeeCordPermissionHandler(BungeeProxiedPlayer player) {
        this.player = player;
        this.design = new BungeeCordPlayerDesign(player);
    }

    @Override
    public boolean isCached() {
        return true;//Unused
    }

    @Override
    public boolean setCached(boolean b) {
        return false;//Unused
    }

    @Override
    public PermissionHandler reload() {
        return this;
    }

    @Override
    public String getPrimaryGroup() {
        if(player.getOriginal().getGroups().size() > 0){
            return player.getOriginal().getGroups().iterator().next();
        }else {
            return null;
        }
    }

    @Override
    public Collection<String> getGroups() {
        return player.getOriginal().getGroups();
    }

    @Override
    public Collection<String> getPermissions() {
        return player.getOriginal().getPermissions();
    }

    @Override
    public Collection<String> getEffectivePermissions() {
        return player.getOriginal().getPermissions();
    }

    @Override
    public PlayerDesign getDesign() {
        return design;
    }

    @Override
    public PlayerDesign getDesign(MinecraftPlayer forPlayer) {
        return design;
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return player.getOriginal().getPermissions().contains(permission);
    }

    @Override
    public boolean isPermissionAssigned(String permission) {
        return player.getOriginal().getPermissions().contains(permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.getOriginal().hasPermission(permission);
    }

    @Override
    public PermissionResult hasPermissionExact(String permission) {
        return player.getOriginal().hasPermission(permission) ? PermissionResult.ALLOWED : PermissionResult.DENIED;
    }

    @Override
    public void setPermission(String permission, boolean allowed) {
        player.getOriginal().setPermission(permission,allowed);
    }

    @Override
    public void unsetPermission(String permission) {
        player.getOriginal().getPermissions().remove(permission);
    }

    @Override
    public void addGroup(String name) {
        player.getOriginal().addGroups(name);
    }

    @Override
    public void removeGroup(String name) {
        player.getOriginal().removeGroups(name);
    }

    @Override
    public boolean isOperator() {
        throw new UnsupportedOperationException("BungeeCord does not support operators");
    }

    @Override
    public void setOperator(boolean operator) {
        throw new UnsupportedOperationException("BungeeCord does not support operators");
    }

    @Override
    public void onHandlerInitialized() {
        player.setDesign(design);
    }
}
