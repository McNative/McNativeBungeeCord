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

import de.dytanic.cloudnet.api.CloudAPI;
import de.dytanic.cloudnet.api.network.packet.out.PacketOutLogoutPlayer;
import de.dytanic.cloudnet.bridge.CloudProxy;
import de.dytanic.cloudnet.bridge.event.proxied.ProxiedSubChannelMessageEvent;
import de.dytanic.cloudnet.lib.player.CloudPlayer;
import de.dytanic.cloudnet.lib.proxylayout.ProxyConfig;
import de.dytanic.cloudnet.lib.server.ProxyGroup;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.service.local.LocalServiceMaxPlayerCountEvent;
import org.mcnative.runtime.bungeecord.McNativeLauncher;
import org.mcnative.runtime.network.integrations.cloudnet.v2.CloudNetV2Messenger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CloudNetV2PlatformListener implements Listener {

    private final CloudNetV2Messenger messenger;
    private final ProxyGroup proxyGroup;

    public CloudNetV2PlatformListener(CloudNetV2Messenger messenger) {
        this.messenger = messenger;
        ProxyServer.getInstance().getPluginManager().registerListener(McNativeLauncher.getPlugin(),this);
        McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM,this);
        proxyGroup = CloudAPI.getInstance().getProxyGroupData(CloudAPI.getInstance().getGroup());
    }

    @net.pretronic.libraries.event.Listener
    public void onMaxCount(LocalServiceMaxPlayerCountEvent event){
        ProxyConfig proxyConfig = proxyGroup.getProxyConfig();
        int onlineCount = CloudAPI.getInstance().getOnlineCount();
        int max = (proxyConfig.getAutoSlot().isEnabled() ? onlineCount + proxyConfig.getAutoSlot()
                .getDynamicSlotSize() : proxyConfig.getMaxPlayers());
        event.setMaxPlayerCount(max);
    }

    @EventHandler
    public void onMessageReceive(ProxiedSubChannelMessageEvent event){
        this.messenger.handleMessageEvent(event.getChannel(),event.getMessage(),event.getDocument());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPostLogin(LoginEvent event){
        //Fixes an issue where players are not logged out while login fails
        UUID uuid = event.getConnection().getUniqueId();
        ProxyServer.getInstance().getScheduler().schedule(McNativeLauncher.getPlugin(), () -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            if(player == null){
                CloudPlayer cloudPlayer = CloudProxy.getInstance().getCloudPlayers().get(uuid);
                if(cloudPlayer != null) {
                    CloudAPI.getInstance().getNetworkConnection().sendPacket(new PacketOutLogoutPlayer(cloudPlayer, uuid));
                }
                CloudProxy.getInstance().getCloudPlayers().remove(uuid);
                ProxyServer.getInstance().getScheduler().schedule(de.dytanic.cloudnet.bridge.CloudProxy.getInstance().getPlugin(),
                        () -> CloudProxy.getInstance().update(), 250L, TimeUnit.MILLISECONDS);
            }
        }, 750L, TimeUnit.MILLISECONDS);
    }
}
