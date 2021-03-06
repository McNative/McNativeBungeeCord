/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.08.19, 15:16
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

package org.mcnative.runtime.bungeecord;

import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.service.local.LocalServiceStartupEvent;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.proxy.ProxyService;
import org.mcnative.runtime.common.event.service.local.DefaultLocalServiceStartupEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class McNativeConfigurationAdapter implements ConfigurationAdapter {

    private final ConfigurationAdapter original;

    public McNativeConfigurationAdapter(ConfigurationAdapter original) {
        this.original = original;
    }

    @Override
    public void load() {
        this.original.load();

        //Clear wrong property configuration
        List<String> servers = Iterators.map(original.getServers().keySet(), s -> s.trim().toLowerCase());
        for (ListenerInfo listener : original.getListeners()) {
            if(listener.getServerPriority() != null){
                Iterators.remove(listener.getServerPriority(), server -> !servers.contains(server.trim().toLowerCase()));
            }
        }

        McNativeBungeeCordConfiguration.SERVER_SERVERS.forEach((name, config) -> {
            MinecraftServer server = ProxyService.getInstance().registerServer(name,config.getAddress());
            if(config.getPermission() != null) server.setPermission(config.getPermission());
            if(config.getType() != null) server.setType(config.getType());
        });

        //Config loaded and service is ready
        McNative.getInstance().getLocal().getEventBus().callEvent(LocalServiceStartupEvent.class
                ,new DefaultLocalServiceStartupEvent());
    }



    @Override
    public int getInt(String path, int def) {
        return original.getInt(path,def);
    }

    @Override
    public String getString(String path, String def) {
        return original.getString(path, def);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        if(path.equalsIgnoreCase("log_pings")) return false;//Disables ping requests in BungeeCord (Better for Support)
        else return original.getBoolean(path, def);
    }

    @Override
    public Collection<?> getList(String path, Collection<?> def) {
        return original.getList(path, def);
    }

    @Override
    public Map<String, ServerInfo> getServers() {
        return original.getServers();
    }

    @Override
    public Collection<ListenerInfo> getListeners() {
        return original.getListeners();
    }

    @Override
    public Collection<String> getGroups(String player) {
        return original.getGroups(player);
    }

    @Override
    public Collection<String> getPermissions(String group) {
        return original.getPermissions(group);
    }
}
