/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.07.20, 11:35
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

package org.mcnative.runtime.bungeecord.network.cloudnet;

import de.dytanic.cloudnet.ext.bridge.BridgeHelper;
import de.dytanic.cloudnet.ext.bridge.bungee.BungeeCloudNetHelper;
import de.dytanic.cloudnet.ext.bridge.bungee.event.BungeeChannelMessageReceiveEvent;
import de.dytanic.cloudnet.ext.bridge.proxy.BridgeProxyHelper;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.mcnative.runtime.bungeecord.McNativeLauncher;
import org.mcnative.runtime.network.integrations.cloudnet.v3.CloudNetV3Messenger;

import java.util.concurrent.TimeUnit;

public class CloudNetV3PlatformListener implements Listener {

    private final CloudNetV3Messenger messenger;

    public CloudNetV3PlatformListener(CloudNetV3Messenger messenger) {
        this.messenger = messenger;
        ProxyServer.getInstance().getPluginManager().registerListener(McNativeLauncher.getPlugin(),this);
    }

    @EventHandler
    public void onMessageReceive(BungeeChannelMessageReceiveEvent event){
        this.messenger.handleMessageEvent(event.getChannel(),event.getMessage(),event.getData());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPostLogin(PostLoginEvent event){
        if(event.getPlayer().getServer() == null){
            ProxiedPlayer player = event.getPlayer();
            boolean available = BridgeProxyHelper.getNextFallback(player.getUniqueId(), "null", player::hasPermission).isPresent();
            if(!available){
                event.getPlayer().disconnect(ProxyServer.getInstance().getTranslation("fallback_kick"));
                ProxyServer.getInstance().getScheduler().schedule(McNativeLauncher.getPlugin(), () -> {
                    BridgeHelper.sendChannelMessageProxyDisconnect(BungeeCloudNetHelper.createNetworkConnectionInfo(event.getPlayer().getPendingConnection()));
                    BridgeProxyHelper.clearFallbackProfile(event.getPlayer().getUniqueId());
                    ProxyServer.getInstance().getScheduler().schedule(McNativeLauncher.getPlugin(), BridgeHelper::updateServiceInfo, 150, TimeUnit.MILLISECONDS);
                }, 250L, TimeUnit.MILLISECONDS);
            }
        }
    }

}
