package org.mcnative.runtime.bungeecord;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.client.CustomPluginMessageListener;

public class BungeeCustomPluginMessage implements Listener {

    private final ObjectOwner owner;
    private final String channel;
    private final CustomPluginMessageListener listener;

    public BungeeCustomPluginMessage(ObjectOwner owner,String channel, CustomPluginMessageListener listener) {
        this.owner = owner;
        this.channel = channel;
        this.listener = listener;
    }

    public ObjectOwner getOwner() {
        return owner;
    }

    public String getChannel() {
        return channel;
    }

    public CustomPluginMessageListener getListener() {
        return listener;
    }

    public void onReceive(PluginMessageEvent event){
        if(event.getSender() instanceof ProxiedPlayer && event.getTag().equalsIgnoreCase(channel)){
            ConnectedMinecraftPlayer player = McNative.getInstance().getLocal().getConnectedPlayer(((ProxiedPlayer) event.getSender()).getUniqueId());
            listener.onReceive(player,channel,event.getData());
        }
    }

}
