/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 10.01.20, 20:33
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

import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.chat.ComponentSerializer;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.io.IORuntimeException;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.network.component.server.ServerVersion;
import org.mcnative.runtime.api.protocol.MinecraftEdition;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import org.mcnative.runtime.api.text.Text;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.TextComponent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BungeeCordServerStatusResponse implements ServerStatusResponse {

    private final static TextComponent NEXT_LINE_COMPONENT = Text.of("\n");

    private ServerPing ping;
    private int ping0;

    public BungeeCordServerStatusResponse(ServerPing ping) {
        this.ping = ping;
    }

    public ServerPing getOriginal() {
        return ping;
    }

    public void setPing(ServerPing ping) {
        this.ping = ping;
    }

    @Override
    public ServerVersion getVersion() {
        return new ServerVersion(ping.getVersion().getName(),MinecraftProtocolVersion.of(MinecraftEdition.JAVA,ping.getVersion().getProtocol()));
    }

    @Override
    public ServerStatusResponse setVersion(ServerVersion version) {
        ping.setVersion(new ServerPing.Protocol(version.getName(),version.getProtocol().getNumber()));
        return this;
    }

    @Override
    public ServerStatusResponse setVersion(String name, MinecraftProtocolVersion protocol) {
        ping.setVersion(new ServerPing.Protocol(name,protocol.getNumber()));
        return this;
    }

    @Override
    public MessageComponent<?> getDescription() {
        return Text.decompile(ComponentSerializer.toString(ping.getDescriptionComponent()));
    }

    @Override
    public VariableSet getDescriptionVariables() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    @Override
    public ServerStatusResponse setDescription(MessageComponent<?> description,VariableSet variables) {
        ping.setDescriptionComponent(ComponentSerializer.parse(description.compileToString(MinecraftProtocolVersion.getLatest(MinecraftEdition.JAVA),variables))[0]);
        return this;
    }

    @Override
    public ServerStatusResponse setDescription(MessageComponent<?> line1, MessageComponent<?> line2, VariableSet variables) {
        TextComponent component = Text.of("");
        component.addExtra(line1);
        component.addExtra(NEXT_LINE_COMPONENT);
        component.addExtra(line2);
        setDescription(component);
        return this;
    }

    @Override
    public String getFavicon() {
        return ping.getFaviconObject().getEncoded();
    }

    @Override
    public ServerStatusResponse setFavicon(String favicon) {
        ping.setFavicon(favicon);
        return this;
    }

    @Override
    public ServerStatusResponse setFavicon(BufferedImage image) {
        ping.setFavicon(Favicon.create(image));
        return this;
    }

    @Override
    public ServerStatusResponse setFavicon(URL url) {
        try {
            return setFavicon(ImageIO.read(url));
        } catch (IOException exception) {
            throw new IORuntimeException(exception);
        }
    }

    @Override
    public ServerStatusResponse setFavicon(File location) {
        try {
            return setFavicon(ImageIO.read(location));
        } catch (IOException exception) {
            throw new IORuntimeException(exception);
        }
    }

    @Override
    public int getMaxPlayers() {
        return ping.getPlayers().getMax();
    }

    @Override
    public ServerStatusResponse setMaxPlayers(int maxPlayers) {
        ping.getPlayers().setMax(maxPlayers);
        return this;
    }

    @Override
    public int getOnlinePlayers() {
        return ping.getPlayers().getOnline();
    }

    @Override
    public ServerStatusResponse setOnlinePlayers(int onlinePlayers) {
        ping.getPlayers().setMax(onlinePlayers);
        return this;
    }

    @Override
    public List<PlayerInfo> getPlayerInfo() {
        ServerPing.PlayerInfo[] players = ping.getPlayers().getSample();
        List<PlayerInfo> result = new ArrayList<>(players.length);
        for (ServerPing.PlayerInfo player : players) result.add(map(player));
        return Collections.unmodifiableList(result);
    }

    @Override
    public ServerStatusResponse setPlayerInfo(List<PlayerInfo> playerInfo) {
        ServerPing.PlayerInfo[] info = new ServerPing.PlayerInfo[playerInfo.size()];
        for (int i = 0; i < playerInfo.size(); i++) info[i] = map(playerInfo.get(i));
        ping.getPlayers().setSample(info);
        return this;
    }

    @Override
    public ServerStatusResponse setPlayerInfo(PlayerInfo[] playerInfo) {
        ServerPing.PlayerInfo[] info = new ServerPing.PlayerInfo[playerInfo.length];
        for (int i = 0; i < playerInfo.length; i++) info[i] = map(playerInfo[i]);
        ping.getPlayers().setSample(info);
        return this;
    }

    @Override
    public ServerStatusResponse addPlayerInfo(PlayerInfo info) {
        ServerPing.PlayerInfo[] current = ping.getPlayers().getSample();
        if(current == null) current = new ServerPing.PlayerInfo[0];

        ServerPing.PlayerInfo[] updated = new ServerPing.PlayerInfo[current.length+1];
        System.arraycopy(current, 0, updated, 0, current.length);

        updated[current.length] = map(info);
        ping.getPlayers().setSample(updated);
        return this;
    }

    @Override
    public ServerStatusResponse addPlayerInfo(String text) {
        addPlayerInfo(ServerStatusResponse.newPlayerInfo(text));
        return this;
    }

    @Override
    public ServerStatusResponse removePlayerInfo(PlayerInfo info) {
        ServerPing.PlayerInfo[] current = ping.getPlayers().getSample();
        List<ServerPing.PlayerInfo> updated = new ArrayList<>();
        for (ServerPing.PlayerInfo player : current) if(!player.equals(info)) updated.add(player);
        ping.getPlayers().setSample(updated.toArray(new ServerPing.PlayerInfo[0]));
        return this;
    }

    @Override
    public ServerStatusResponse clearPlayerInfo() {
        ping.getPlayers().setSample(new ServerPing.PlayerInfo[]{});
        return this;
    }

    @Override
    public int getPing() {
        return ping0;
    }

    @Override
    public void setPing(int ping) {
        ping0 = ping;
    }

    @Override
    public ServerStatusResponse clone() {
        throw new UnsupportedOperationException("Currently not supported");
    }

    private ServerPing.PlayerInfo map(PlayerInfo info){
        if(info instanceof ServerPing.PlayerInfo) return (ServerPing.PlayerInfo) info;
        return new DefaultPlayerInfo(info.getName(),info.getUniqueId());
    }

    private PlayerInfo map(ServerPing.PlayerInfo info){
        if(info instanceof PlayerInfo) return (PlayerInfo) info;
        else return new DefaultPlayerInfo(info.getName(),info.getUniqueId());
    }

    public static class DefaultPlayerInfo extends ServerPing.PlayerInfo implements PlayerInfo {

        public DefaultPlayerInfo(String name) {
            super(name,UUID.randomUUID());
        }

        public DefaultPlayerInfo(String name, String id) {
            super(name, id);
        }

        public DefaultPlayerInfo(String name, UUID uniqueId) {
            super(name, uniqueId);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this){
                return true;
            }else if(obj instanceof PlayerInfo){
                return ((PlayerInfo) obj).getUniqueId().equals(getUniqueId());
            }else if(obj instanceof ServerPing.PlayerInfo){
                return ((ServerPing.PlayerInfo) obj).getUniqueId().equals(getUniqueId());
            }
            return false;
        }
    }
}
