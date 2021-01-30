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

package org.mcnative.runtime.bungeecord.event;

import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.SkinConfiguration;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.utility.SystemUtil;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerLoginConfirmEvent;
import org.mcnative.runtime.bungeecord.event.player.*;
import org.mcnative.runtime.bungeecord.event.server.BungeeServerConnectEvent;
import org.mcnative.runtime.bungeecord.event.server.BungeeServerConnectedEvent;
import org.mcnative.runtime.bungeecord.event.server.BungeeServerKickEvent;
import org.mcnative.runtime.bungeecord.event.server.BungeeServerSwitchEvent;
import org.mcnative.runtime.bungeecord.player.BungeeCordPlayerManager;
import org.mcnative.runtime.bungeecord.player.BungeePendingConnection;
import org.mcnative.runtime.bungeecord.player.BungeeProxiedPlayer;
import org.mcnative.runtime.bungeecord.player.permission.BungeeCordPermissionHandler;
import org.mcnative.runtime.bungeecord.plugin.McNativeEventBus;
import org.mcnative.runtime.bungeecord.plugin.command.McNativeCommand;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.connection.ConnectionState;
import org.mcnative.runtime.api.event.service.local.LocalServicePingEvent;
import org.mcnative.runtime.api.event.service.local.LocalServiceReloadEvent;
import org.mcnative.runtime.api.event.player.*;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerLoginEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPendingLoginEvent;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPostLoginEvent;
import org.mcnative.runtime.api.event.player.settings.MinecraftPlayerSettingsChangedEvent;
import org.mcnative.runtime.api.event.server.MinecraftPlayerServerSwitchEvent;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.PlayerClientSettings;
import org.mcnative.runtime.api.player.data.MinecraftPlayerData;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.proxy.ProxyService;
import org.mcnative.runtime.api.proxy.ServerConnectHandler;
import org.mcnative.runtime.api.proxy.event.player.MinecraftPlayerServerConnectEvent;
import org.mcnative.runtime.api.proxy.event.player.MinecraftPlayerServerConnectedEvent;
import org.mcnative.runtime.api.proxy.event.player.MinecraftPlayerServerKickEvent;
import org.mcnative.runtime.api.serviceprovider.permission.Permissable;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionHandler;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.common.event.player.DefaultMinecraftPlayerLoginConfirmEvent;
import org.mcnative.runtime.common.event.service.local.DefaultLocalServiceReloadEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class McNativeBridgeEventHandler {

    public static Favicon DEFAULT_FAVICON;

    private final McNativeEventBus pluginManager;
    private final BungeeCordPlayerManager playerManager;
    private final BungeeCordServerMap serverMap;
    private final EventBus eventBus;
    private final Map<UUID, BungeeProxiedPlayer> pendingPlayers;
    private final Map<Connection,String> tabCompleteCursors;

    public McNativeBridgeEventHandler(McNativeEventBus pluginManager, EventBus eventBus, BungeeCordPlayerManager playerManager,BungeeCordServerMap serverMap) {
        this.pluginManager = pluginManager;
        this.eventBus = eventBus;
        this.playerManager = playerManager;
        this.serverMap = serverMap;

        this.pendingPlayers = new ConcurrentHashMap<>();
        this.tabCompleteCursors = new ConcurrentHashMap<>();

        setup();
    }

    private void setup(){
        //Ping
        eventBus.registerMappedClass(LocalServicePingEvent.class,ProxyPingEvent.class);
        pluginManager.registerMangedEvent(ProxyPingEvent.class,this::handleProxyPing);

        //Login
        eventBus.registerMappedClass(MinecraftPlayerLoginEvent.class, LoginEvent.class);
        pluginManager.registerMangedEvent(LoginEvent.class,this::handleLogin);

        //PostLogin
        eventBus.registerMappedClass(MinecraftPlayerPostLoginEvent.class, PostLoginEvent.class);
        pluginManager.registerMangedEvent(PostLoginEvent.class,this::handlePostLogin);

        //Connect Server
        eventBus.registerMappedClass(MinecraftPlayerServerConnectEvent.class, ServerConnectEvent.class);
        pluginManager.registerMangedEvent(ServerConnectEvent.class,this::handleServerConnect);

        //Connected Server
        eventBus.registerMappedClass(MinecraftPlayerServerConnectedEvent.class, ServerConnectedEvent.class);
        pluginManager.registerMangedEvent(ServerConnectedEvent.class,this::handleServerConnected);

        //Switch Server
        eventBus.registerMappedClass(MinecraftPlayerServerSwitchEvent.class, ServerSwitchEvent.class);
        pluginManager.registerMangedEvent(ServerSwitchEvent.class,this::handleServerSwitch);

        //Server Kick
        eventBus.registerMappedClass(MinecraftPlayerServerKickEvent.class, ServerKickEvent.class);
        pluginManager.registerMangedEvent(ServerKickEvent.class,this::handleServerKick);

        //Chat event
        eventBus.registerMappedClass(MinecraftPlayerChatEvent.class, ChatEvent.class);
        eventBus.registerMappedClass(MinecraftPlayerCommandPreprocessEvent.class, ChatEvent.class);
        pluginManager.registerMangedEvent(ChatEvent.class,this::handleChatEvent);

        //Tab complete
        eventBus.registerMappedClass(MinecraftPlayerTabCompleteEvent.class, TabCompleteEvent.class);
        pluginManager.registerMangedEvent(TabCompleteEvent.class,this::handleTabComplete);

        //Tab response
        eventBus.registerMappedClass(MinecraftPlayerTabCompleteResponseEvent.class, TabCompleteResponseEvent.class);
        pluginManager.registerMangedEvent(TabCompleteResponseEvent.class,this::handleTabCompleteResponse);

        //Logout
        eventBus.registerMappedClass(MinecraftPlayerLogoutEvent.class, PlayerDisconnectEvent.class);
        pluginManager.registerMangedEvent(PlayerDisconnectEvent.class,this::handleLogout);

        //Permission
        eventBus.registerMappedClass(org.mcnative.runtime.api.event.PermissionCheckEvent.class, PermissionCheckEvent.class);
        pluginManager.registerMangedEvent(PermissionCheckEvent.class,this::handlePermissionCheck);

        //Settings
        eventBus.registerMappedClass(MinecraftPlayerSettingsChangedEvent.class, SettingsChangedEvent.class);
        pluginManager.registerMangedEvent(SettingsChangedEvent.class,this::handleSettingsChange);

        //Reload
        eventBus.registerMappedClass(LocalServiceReloadEvent.class, ProxyReloadEvent.class);
        pluginManager.registerMangedEvent(ProxyReloadEvent.class,this::handleProxyReload);
    }

    private void handleProxyPing(ProxyPingEvent event) {
        BungeeServerListPingEvent mcNativeEvent = new BungeeServerListPingEvent(event.getConnection(),event);
        if(DEFAULT_FAVICON != null) event.getResponse().setFavicon(DEFAULT_FAVICON);
        ServerStatusResponse defaultResponse = ProxyService.getInstance().getStatusResponse();
        if(defaultResponse != null) mcNativeEvent.setResponse(defaultResponse.clone());
        eventBus.callEvents(ProxyPingEvent.class,event,mcNativeEvent);
    }

    private void handleLogin(LoginEvent event){
        BungeePendingConnection connection = new BungeePendingConnection(event.getConnection());
        connection.setState(ConnectionState.LOGIN);

        MinecraftPlayerPendingLoginEvent pendingEvent = new BungeeMinecraftPendingLoginEvent(connection);
        eventBus.callEvent(MinecraftPlayerPendingLoginEvent.class,pendingEvent);
        if(pendingEvent.isCancelled()){
            connection.disconnect(pendingEvent.getCancelReason(),pendingEvent.getCancelReasonVariables());
            return;
        }

        PlayerDataProvider dataProvider = McNative.getInstance().getRegistry().getService(PlayerDataProvider.class);
        MinecraftPlayerData data = dataProvider.getPlayerData(event.getConnection().getUniqueId());
        if(data == null){
            long now = System.currentTimeMillis();
            data = dataProvider.createPlayerData(
                    event.getConnection().getName()
                    ,event.getConnection().getUniqueId()
                    ,-1
                    ,now
                    ,now
                    ,connection.getGameProfile());
        }else data.updateLoginInformation(connection.getName(),connection.getGameProfile(),System.currentTimeMillis());
        BungeeProxiedPlayer player = new BungeeProxiedPlayer(serverMap,connection,data);

        MinecraftPlayerLoginEvent loginEvent = new BungeeMinecraftLoginEvent(event,connection,player);
        eventBus.callEvents(LoginEvent.class,event,loginEvent);

        if(loginEvent.isCancelled()){
            if(Arrays.equals(event.getCancelReasonComponents(), BungeeMinecraftLoginEvent.MCNATIVE_MANAGER)){
                connection.disconnect(loginEvent.getCancelReason(),loginEvent.getCancelReasonVariables());
                event.setCancelled(false);
            }
        }else{
            connection.setState(ConnectionState.GAME);
            connection.setPlayer(player);
            pendingPlayers.put(player.getUniqueId(),player);
        }
    }

    private void handlePostLogin(PostLoginEvent event){
        BungeeProxiedPlayer player = pendingPlayers.remove(event.getPlayer().getUniqueId());
        if(player == null){
            event.getPlayer().disconnect(TextComponent.fromLegacyText("§cInternal server error."));
            return;
        }
        player.postLogin(event.getPlayer());
        playerManager.registerPlayer(player);
        MinecraftPlayerPostLoginEvent mcNativeEvent = new BungeeMinecraftPostLoginEvent(player);
        eventBus.callEvents(PostLoginEvent.class,event,mcNativeEvent);
    }

    private void handleServerConnect(ServerConnectEvent event){
        ConnectedMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftServer server = serverMap.getMappedServer(event.getTarget());
        if(server.getPermission() != null && !player.hasPermission(server.getPermission())) event.setCancelled(true);
        MinecraftPlayerServerConnectEvent mcNativeEvent = new BungeeServerConnectEvent(event,serverMap,player);
        ServerConnectHandler handler = McNative.getInstance().getRegistry().getServiceOrDefault(ServerConnectHandler.class);
        if(handler != null && player.getServer() == null){
            mcNativeEvent.setTarget(handler.getFallbackServer(player,player.getServer()));
        }
        eventBus.callEvents(ServerConnectEvent.class,event,mcNativeEvent);
        if(handler != null && player.getServer() == null && event.getTarget() == null){
            MessageComponent<?> message = handler.getNoFallBackServerMessage(player);
            if(message != null) player.disconnect(message);
        }
    }

    private void handleServerConnected(ServerConnectedEvent event){
        OnlineMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftServer server = serverMap.getMappedServer(event.getServer().getInfo());
        MinecraftPlayerServerConnectedEvent mcNativeEvent = new BungeeServerConnectedEvent(player,server);

        ((BungeeProxiedPlayer)player).setServer(server);
        ((BungeeProxiedPlayer) player).injectDownstreamProtocolHandlersToPipeline();

        eventBus.callEvents(ServerConnectedEvent.class,event,mcNativeEvent);

        if(((BungeeProxiedPlayer)player).isFirstJoin()) {
            eventBus.callEvent(MinecraftPlayerLoginConfirmEvent.class, new DefaultMinecraftPlayerLoginConfirmEvent(player));
            ((BungeeProxiedPlayer)player).setFirstJoin(false);
        }
    }

    private void handleServerSwitch(ServerSwitchEvent event){
        OnlineMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftServer from = event.getFrom() != null ? serverMap.getMappedServer(event.getFrom()) : null;
        MinecraftPlayerServerSwitchEvent mcNativeEvent = new BungeeServerSwitchEvent(player,from);
        eventBus.callEvents(ServerSwitchEvent.class,event,mcNativeEvent);
    }

    private void handleServerKick(ServerKickEvent event){
        ConnectedMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        MinecraftPlayerServerKickEvent mcNativeEvent = new BungeeServerKickEvent(serverMap,event,player);
        ServerConnectHandler handler = McNative.getInstance().getRegistry().getServiceOrDefault(ServerConnectHandler.class);
        if(handler != null){
            mcNativeEvent.setFallbackServer(handler.getFallbackServer(player,player.getServer()));
        }
        eventBus.callEvents(ServerKickEvent.class,event,mcNativeEvent);
        if(handler != null && event.getCancelServer() == null){
            MessageComponent<?> message = handler.getNoFallBackServerMessage(player);
            if(message != null) mcNativeEvent.setKickReason(message);
        }
    }

    private void handleLogout(PlayerDisconnectEvent event){
        OnlineMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        if(player instanceof BungeeProxiedPlayer) ((BungeeProxiedPlayer) player).handleLogout();
        MinecraftPlayerLogoutEvent mcNativeEvent = new BungeeMinecraftLogoutEvent(player);
        eventBus.callEvents(PlayerDisconnectEvent.class,event,mcNativeEvent);
        SystemUtil.sleepUninterruptible(700);//Sleep for security reasons
        playerManager.unregisterPlayer(event.getPlayer().getUniqueId());
    }

    private void handleChatEvent(ChatEvent event) {
        if(event.getSender() instanceof ProxiedPlayer){
            ConnectedMinecraftPlayer player = playerManager.getMappedPlayer((ProxiedPlayer) event.getSender());
            if(event.isCommand()){
                MinecraftPlayerCommandPreprocessEvent mcNativeEvent = new BungeeMinecraftPlayerCommandPreprocessEvent(event,player);
                eventBus.callEvents(ChatEvent.class,event,mcNativeEvent);
            }else{
                MinecraftPlayerChatEvent mcNativeEvent = new BungeeMinecraftPlayerChatEvent(event,player);
                eventBus.callEvents(ChatEvent.class,event,mcNativeEvent);
                if(!event.isCancelled() && mcNativeEvent.getChannel() != null){
                    event.setCancelled(true);
                    if(mcNativeEvent.getOutputMessage() == null){
                        mcNativeEvent.getChannel().chat(player,mcNativeEvent.getMessage(),mcNativeEvent.getOutputVariables());
                    }else{
                        mcNativeEvent.getChannel().sendMessage(mcNativeEvent.getOutputMessage(),mcNativeEvent.getOutputVariables());
                    }
                    McNative.getInstance().getLogger().info("["+mcNativeEvent.getChannel().getName()+"] "+player.getName()+": "+event.getMessage());
                }
            }

        }else eventBus.callEvent(event);
    }

    private void handleTabComplete(TabCompleteEvent event){
        if(event.getSender() instanceof ProxiedPlayer){
            OnlineMinecraftPlayer player = playerManager.getMappedPlayer((ProxiedPlayer) event.getSender());
            MinecraftPlayerTabCompleteEvent mcNativeEvent = new BungeeTabCompleteEvent(event,player);
            this.tabCompleteCursors.put(event.getSender(),event.getCursor());
            eventBus.callEvents(TabCompleteEvent.class,event,mcNativeEvent);
        }else eventBus.callEvent(event);
    }

    private void handleTabCompleteResponse(TabCompleteResponseEvent event){
        if(event.getReceiver() instanceof ProxiedPlayer){
            OnlineMinecraftPlayer player = playerManager.getMappedPlayer((ProxiedPlayer) event.getReceiver());
            String cursor = this.tabCompleteCursors.get(event.getReceiver());
            MinecraftPlayerTabCompleteResponseEvent mcNativeEvent = new BungeeTabCompleteResponseEvent(event,player,cursor);
            eventBus.callEvents(TabCompleteEvent.class,event,mcNativeEvent);
        }else eventBus.callEvent(event);
    }

    private void handlePermissionCheck(PermissionCheckEvent event){
        PermissionHandler handler = null;
        CommandSender sender;
        if(event.getSender() instanceof ProxiedPlayer){
            OnlineMinecraftPlayer player = playerManager.getMappedPlayer((ProxiedPlayer) event.getSender());
            sender = player;
            handler = player.getPermissionHandler();
        }else if(event.getSender().equals(ProxyServer.getInstance().getConsole())){
            sender = McNative.getInstance().getConsoleSender();
        }else {
            if(event.getSender() instanceof Permissable){
                handler = ((Permissable) event.getSender()).getPermissionHandler();
            }else if(event.getSender() instanceof PermissionHandler){
                handler = ((PermissionHandler) event.getSender());
            }
            sender = new McNativeCommand.MappedCommandSender(event.getSender());
        }

        if(handler != null && !(handler instanceof BungeeCordPermissionHandler)){
            event.setHasPermission(handler.hasPermission(event.getPermission()));
        }

        BungeePermissionCheckEvent mcNativeEvent = new BungeePermissionCheckEvent(event,sender,handler);
        eventBus.callEvents(PermissionCheckEvent.class,event,mcNativeEvent);
    }

    private void handleProxyReload(ProxyReloadEvent event){
        LocalServiceReloadEvent mcNativeEvent = new DefaultLocalServiceReloadEvent();
        eventBus.callEvents(ProxyReloadEvent.class,event,mcNativeEvent);
    }

    private void handleSettingsChange(SettingsChangedEvent event){
        ConnectedMinecraftPlayer player = playerManager.getMappedPlayer(event.getPlayer());
        PlayerClientSettings settings = mapSettings(event.getPlayer());
        MinecraftPlayerSettingsChangedEvent mcNativeEvent = new BungeeMinecraftPlayerSettingsChangedEvent(player,settings);
        eventBus.callEvents(PermissionCheckEvent.class,event,mcNativeEvent);
        ((BungeeProxiedPlayer)player).setSettings(settings);
    }

    private PlayerClientSettings mapSettings(ProxiedPlayer player){
        return new PlayerClientSettings(player.getLocale()
                ,player.getViewDistance()
                , PlayerClientSettings.ChatMode.valueOf(player.getChatMode().name())
                ,player.hasChatColors()
                ,mapSkinPart(player)
                , PlayerClientSettings.MainHand.valueOf(player.getMainHand().name()));
    }

    private PlayerClientSettings.SkinParts mapSkinPart(ProxiedPlayer player){
        SkinConfiguration skin = player.getSkinParts();
        if(skin.getClass().getName().equals("net.md_5.bungee.PlayerSkinConfiguration")){
            return new PlayerClientSettings.SkinParts(ReflectionUtil.getFieldValue(skin,"bitmask",Byte.class));
        }else{
            return PlayerClientSettings.SkinParts.SKIN_SHOW_ALL;
        }
    }

}