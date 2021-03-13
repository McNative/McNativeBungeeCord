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

import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import de.dytanic.cloudnet.ext.bridge.bungee.event.BungeeChannelMessageReceiveEvent;
import de.dytanic.cloudnet.ext.bridge.proxy.BridgeProxyHelper;
import de.dytanic.cloudnet.ext.bridge.proxy.PlayerFallback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.mcnative.runtime.bungeecord.McNativeLauncher;
import org.mcnative.runtime.network.integrations.cloudnet.v3.CloudNetV3Messenger;

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
    public void onPlayerPostLogin(PreLoginEvent event){
        boolean available = BridgeProxyHelper.getFallbacks()
                .flatMap(proxyFallback -> BridgeProxyHelper.getCachedServiceInfoSnapshots(proxyFallback.getTask())
                        .map(serviceInfoSnapshot -> new PlayerFallback(proxyFallback.getPriority(), serviceInfoSnapshot)))
                .anyMatch(fallback -> fallback.getTarget().getProperty(BridgeServiceProperty.IS_ONLINE).orElse(false));
        if(!available){
            event.setCancelled(true);
            event.setCancelReason(ProxyServer.getInstance().getTranslation("fallback_kick","No servers avaialble"));
        }
    }

}
