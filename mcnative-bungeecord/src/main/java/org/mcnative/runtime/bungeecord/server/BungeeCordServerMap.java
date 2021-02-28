/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.08.19, 15:27
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

package org.mcnative.runtime.bungeecord.server;

import gnu.trove.function.TObjectFunction;
import gnu.trove.map.TMap;
import gnu.trove.procedure.TObjectObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import net.md_5.bungee.api.config.ServerInfo;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.MinecraftServerType;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Consumer;

public class BungeeCordServerMap implements TMap<String, ServerInfo> {

    private final Set<ServerEntry> servers;

    public BungeeCordServerMap() {
        this.servers = new HashSet<>();
    }

    @Override
    public int size() {
        return servers.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return servers.contains(value);
    }

    @Override
    public ServerInfo get(Object name) {
        if(name == null) return null;
        ServerEntry result = Iterators.findOne(this.servers, entry -> entry.getKey().equalsIgnoreCase(name.toString()));
        if(result != null) return result.bungeeCord;
        else return null;
    }

    public Collection<MinecraftServer> getServers(){
        return Iterators.map(this.servers, entry -> entry.mcNative);
    }

    public Collection<MinecraftServer> getServers(MinecraftServerType type){
        Validate.notNull(type);
        return Iterators.map(this.servers, entry -> entry.mcNative, entry -> entry.mcNative.getType().equals(type));
    }

    public MinecraftServer getServer(String name){
        Validate.notNull(name);
        ServerEntry result = Iterators.findOne(this.servers, entry -> entry.getKey().equalsIgnoreCase(name));
        if(result != null) return result.mcNative;
        else return null;
    }

    public MinecraftServer getServer(UUID uniqueId){
        Validate.notNull(uniqueId);
        ServerEntry result = Iterators.findOne(this.servers, entry -> entry.mcNative.getIdentifier().getUniqueId().equals(uniqueId));
        if(result != null) return result.mcNative;
        else return null;
    }

    public MinecraftServer getServer(InetSocketAddress address){
        Validate.notNull(address);
        ServerEntry result = Iterators.findOne(this.servers, entry -> entry.getValue().getAddress().equals(address));
        if(result != null) return result.mcNative;
        else return null;
    }

    public MinecraftServer getMappedServer(ServerInfo info){
        Validate.notNull(info);
        if(info instanceof MinecraftServer) return (MinecraftServer) info;

        ServerEntry result = Iterators.findOne(this.servers, entry -> entry.bungeeCord.getName().equalsIgnoreCase(info.getName()));
        if(result == null) throw new IllegalArgumentException("McNative mapping error (BungeeCord -> McNative)");
         return result.mcNative;
    }

    public ServerInfo getMappedInfo(MinecraftServer server){
        Validate.notNull(server);
        if(server instanceof ServerInfo) return (ServerInfo) server;
        ServerEntry result = Iterators.findOne(this.servers, entry -> entry.mcNative.equals(server));
        if(result == null) throw new IllegalArgumentException("The targeted server is not registered as a server (McNative -> BungeeCord).");
        return result.bungeeCord;
    }

    @Override
    public ServerInfo put(String unused, ServerInfo value) {
        if(unused != null && !unused.equalsIgnoreCase(value.getName())) throw new IllegalArgumentException("Key does not match with the server name");
        MinecraftServer server = value instanceof MinecraftServer ? (MinecraftServer) value : new WrappedBungeeMinecraftServer(value);
        Iterators.removeOne(this.servers, entry -> entry.getKey().equalsIgnoreCase(value.getName()));
        this.servers.add(new ServerEntry(server,value));
        return value;
    }

    @Override
    public ServerInfo remove(Object key) {
        ServerEntry server = Iterators.removeOne(this.servers, server1 -> server1.getKey().equals(key));
        return server != null ? server.bungeeCord : null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends ServerInfo> map) {
        Validate.notNull(map);
        map.values().forEach((Consumer<ServerInfo>) info -> put(null,info));
    }

    @Override
    public void clear() {
        this.servers.clear();
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        servers.forEach(server -> keys.add(server.getKey()));
        return keys;
    }

    @Override
    public Collection<ServerInfo> values() {
        return Iterators.map(this.servers, entry -> entry.bungeeCord);
    }

    @Override
    public Set<Entry<String, ServerInfo>> entrySet() {
        return new HashSet<>(servers);
    }

    @Override
    public ServerInfo putIfAbsent(String s, ServerInfo serverInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forEachKey(TObjectProcedure<? super String> tObjectProcedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forEachValue(TObjectProcedure<? super ServerInfo> tObjectProcedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forEachEntry(TObjectObjectProcedure<? super String, ? super ServerInfo> tObjectObjectProcedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainEntries(TObjectObjectProcedure<? super String, ? super ServerInfo> tObjectObjectProcedure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transformValues(TObjectFunction<ServerInfo, ServerInfo> tObjectFunction) {
        throw new UnsupportedOperationException();
    }

    private static class ServerEntry implements Entry<String, ServerInfo>{

        private MinecraftServer mcNative;
        private ServerInfo bungeeCord;

        public ServerEntry(MinecraftServer mcNative, ServerInfo bungeeCord) {
            this.mcNative = mcNative;
            this.bungeeCord = bungeeCord;
        }

        @Override
        public String getKey() {
            return bungeeCord.getName();
        }

        @Override
        public ServerInfo getValue() {
            return bungeeCord;
        }

        @Override
        public ServerInfo setValue(ServerInfo value) {
            this.bungeeCord = value;
            this.mcNative = value instanceof MinecraftServer ? (MinecraftServer) value : new WrappedBungeeMinecraftServer(value);
            return bungeeCord;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || bungeeCord.equals(obj) || mcNative.equals(obj);
        }
    }
}
