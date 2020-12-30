/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.12.19, 15:25
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

package org.mcnative.runtime.bungeecord.plugin.command;


import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.pretronic.libraries.command.command.Command;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.bungeecord.player.BungeeProxiedPlayer;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.plugin.CustomCommandSender;
import org.mcnative.runtime.api.serviceprovider.permission.Permissable;
import org.mcnative.runtime.api.text.Text;

import java.util.Collection;
import java.util.Collections;

public class BungeeCordCommand implements Command {

    private final net.md_5.bungee.api.plugin.Command original;
    private final ObjectOwner owner;
    private final CommandConfiguration configuration;

    public BungeeCordCommand(net.md_5.bungee.api.plugin.Command original, ObjectOwner owner) {
        this.original = original;
        this.owner = owner;
        this.configuration = CommandConfiguration.newBuilder()
                .name(original.getName())
                .permission(original.getPermission())
                .aliases(original.getAliases())
                .create();
    }

    public net.md_5.bungee.api.plugin.Command getOriginal() {
        return original;
    }


    @Override
    public ObjectOwner getOwner() {
        return owner;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void execute(CommandSender sender, String[] arguments) {
        net.md_5.bungee.api.CommandSender mapped;
        if(sender.equals(McNative.getInstance().getConsoleSender())){
            mapped = ProxyServer.getInstance().getConsole();
        }else if(sender instanceof BungeeProxiedPlayer){
            mapped = ((BungeeProxiedPlayer) sender).getOriginal();
        }else mapped = new MappedCommandSender(sender);
        original.execute(mapped,arguments);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else return original.equals(obj);
    }

    public final static class MappedCommandSender implements net.md_5.bungee.api.CommandSender, CustomCommandSender {

        private final CommandSender original;

        public MappedCommandSender(CommandSender original) {
            this.original = original;
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public void sendMessage(String message) {
            original.sendMessage(message);
        }

        @Override
        public void sendMessages(String... messages) {
            for (String message : messages) sendMessage(message);
        }

        @Override
        public void sendMessage(BaseComponent... component) {
            if(original instanceof OnlineMinecraftPlayer){
                ((OnlineMinecraftPlayer) original).sendMessage(Text.of(ComponentSerializer.toString(component)));
            }else{
                original.sendMessage(BaseComponent.toPlainText(component));
            }
        }

        @Override
        public void sendMessage(BaseComponent baseComponent) {
            if(original instanceof OnlineMinecraftPlayer){
                ((OnlineMinecraftPlayer) original).sendMessage(Text.of(ComponentSerializer.toString(baseComponent)));
            }else{
                original.sendMessage(baseComponent.toPlainText());
            }
        }

        @Override
        public Collection<String> getGroups() {
            if(original instanceof Permissable) return ((Permissable) original).getGroups();
            else return Collections.emptyList();
        }

        @Override
        public void addGroups(String... groups) {
            if(original instanceof Permissable) for (String group : groups) ((Permissable) original).addGroup(group);
        }

        @Override
        public void removeGroups(String... groups) {
            if(original instanceof Permissable) for (String group : groups) ((Permissable) original).removeGroup(group);
        }

        @Override
        public boolean hasPermission(String permission) {
            return original.hasPermission(permission);
        }

        @Override
        public void setPermission(String permission, boolean value) {
            if(original instanceof Permissable){
                ((Permissable) original).setPermission(permission,value);
            }
        }

        @Override
        public Collection<String> getPermissions() {
            if(original instanceof Permissable) return ((Permissable) original).getEffectivePermissions();
            else return Collections.emptyList();
        }

        @Override
        public Object getOriginal() {
            return original;
        }

        @Override
        public Class<?> getOriginalClass() {
            return original.getClass();
        }

        @Override
        public boolean instanceOf(Class<?> originalClass) {
            return original.getClass().isAssignableFrom(originalClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T to(Class<T> originalClass) {
            return (T) original;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                    || (obj instanceof CustomCommandSender && original.equals(((CustomCommandSender) obj).getOriginal()))
                    || original.equals(obj);
        }

        @Override
        public String toString() {
            return original.toString();
        }
    }
}
