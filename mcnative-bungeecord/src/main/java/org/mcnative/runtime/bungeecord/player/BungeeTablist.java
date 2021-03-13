package org.mcnative.runtime.bungeecord.player;

import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.common.player.tablist.AbstractTablist;

public class BungeeTablist extends AbstractTablist {

    @Override
    public String getPlayerTablistNames(ConnectedMinecraftPlayer player, TablistEntry entry) {
        return ((BungeeProxiedPlayer) player).getTablistTeamNames().get(entry);
    }

    @Override
    public int getTablistTeamIndexAndIncrement(ConnectedMinecraftPlayer player) {
        return ((BungeeProxiedPlayer)player).getTablistTeamIndexAndIncrement();
    }

    @Override
    public void putTablistNames(ConnectedMinecraftPlayer player, TablistEntry entry, String teamName) {
        ((BungeeProxiedPlayer) player).getTablistTeamNames().put(entry,teamName);
    }

    @Override
    public void removeTablistNames(ConnectedMinecraftPlayer player, TablistEntry entry) {
        ((BungeeProxiedPlayer) player).getTablistTeamNames().remove(entry);
    }

}
