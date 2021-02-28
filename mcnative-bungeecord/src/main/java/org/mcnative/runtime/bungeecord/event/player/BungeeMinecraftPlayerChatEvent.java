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

import net.md_5.bungee.api.event.ChatEvent;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.api.event.player.MinecraftPlayerChatEvent;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.MinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class BungeeMinecraftPlayerChatEvent implements MinecraftPlayerChatEvent {

    private final ChatEvent original;
    private final OnlineMinecraftPlayer player;
    private ChatChannel channel;

    private MessageComponent<?> outputMessage;
    private VariableSet outputVariables;

    public BungeeMinecraftPlayerChatEvent(ChatEvent original, ConnectedMinecraftPlayer player) {
        this.original = original;
        this.player = player;
        this.channel = player.getPrimaryChatChannel();
    }

    @Override
    public ChatChannel getChannel() {
        return channel;
    }

    @Override
    public void setChannel(ChatChannel channel) {
        this.channel = channel;
    }

    @Override
    public String getMessage() {
        return original.getMessage();
    }

    @Override
    public void setMessage(String message) {
        original.setMessage(message);
    }

    @Override
    public MessageComponent<?> getOutputMessage() {
        return outputMessage;
    }

    @Override
    public void setOutputMessage(MessageComponent<?> outputMessage) {
        Validate.notNull(outputMessage);
        this.outputMessage = outputMessage;
    }

    @Override
    public VariableSet getOutputVariables() {
        return outputVariables;
    }

    @Override
    public void setOutputVariables(VariableSet variables) {
        Validate.notNull(variables);
        this.outputVariables = variables;
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
