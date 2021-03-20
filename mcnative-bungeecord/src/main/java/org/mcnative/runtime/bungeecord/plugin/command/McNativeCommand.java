/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.12.19, 15:26
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

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.pretronic.libraries.command.Completable;
import net.pretronic.libraries.command.NoPermissionAble;
import net.pretronic.libraries.command.NoPermissionHandler;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.bungeecord.player.BungeeCordPlayerManager;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.CustomCommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class McNativeCommand extends Command implements TabExecutor {

    private final CommandManager commandManager;
    private final net.pretronic.libraries.command.command.Command original;

    public McNativeCommand(CommandManager commandManager, net.pretronic.libraries.command.command.Command original) {
        super(original.getConfiguration().getName()
                ,original.getConfiguration().getPermission()
                ,original.getConfiguration().getAliases());
        Validate.notNull(commandManager, original);
        this.commandManager = commandManager;
        this.original = original;
    }

    public net.pretronic.libraries.command.command.Command getOriginal() {
        return original;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        McNative.getInstance().getScheduler().createTask(ObjectOwner.SYSTEM).async().execute(() -> {
            net.pretronic.libraries.command.sender.CommandSender mappedSender = getMappedSender(sender);

            NoPermissionHandler noPermissionHandler;
            if(original instanceof NoPermissionAble) {
                noPermissionHandler = ((NoPermissionAble)original);
            } else {
                noPermissionHandler = commandManager.getNoPermissionHandler(getOriginal().getOwner());
            }

            if(CommandManager.hasPermission(mappedSender, noPermissionHandler, null, original.getConfiguration().getPermission(),
                    original.getConfiguration().getName(), args)) {
                original.execute(mappedSender,args);
            }
        });
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(original instanceof Completable){
            Collection<String> result = ((Completable) original).complete(getMappedSender(sender),args);
            if(result != null){
                if(result instanceof List) return result;
                else return new ArrayList<>(result);
            }
        }
        return Collections.emptyList();
    }

    private net.pretronic.libraries.command.sender.CommandSender getMappedSender(CommandSender sender){
        if(sender.equals(ProxyServer.getInstance().getConsole())){
            return McNative.getInstance().getConsoleSender();
        }else if(sender instanceof ProxiedPlayer) {
            return ((BungeeCordPlayerManager) McNative.getInstance().getPlayerManager()).getMappedPlayer((ProxiedPlayer) sender);
        }else{
            return new MappedCommandSender(sender);
        }
    }

    public final static class MappedCommandSender implements CustomCommandSender {

        private final CommandSender original;

        public MappedCommandSender(CommandSender original) {
            this.original = original;
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
        public String getName() {
            return original.getName();
        }

        @Override
        public boolean hasPermission(String permission) {
            return original.hasPermission(permission);
        }

        @Override
        public void sendMessage(String message) {
            original.sendMessage(message);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                    || (obj instanceof CustomCommandSender && original.equals(((CustomCommandSender) obj).getOriginal()))
                    ||  original.equals(obj);
        }

        @Override
        public String toString() {
            return original.toString();
        }
    }
}
