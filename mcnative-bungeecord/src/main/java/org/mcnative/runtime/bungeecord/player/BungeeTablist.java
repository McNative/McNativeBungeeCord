package org.mcnative.runtime.bungeecord.player;

import org.mcnative.runtime.api.player.OnlineMinecraftPlayer;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.common.player.tablist.AbstractTablist;

public class BungeeTablist extends AbstractTablist {

    @Override
    public String getPlayerTablistNames(OnlineMinecraftPlayer onlineMinecraftPlayer, TablistEntry entry) {
        return null;
    }

    @Override
    public int getTablistTeamIndexAndIncrement(OnlineMinecraftPlayer onlineMinecraftPlayer) {
        return 0;
    }

    @Override
    public String putTablistNames(OnlineMinecraftPlayer onlineMinecraftPlayer, TablistEntry tablistEntry, String s) {
        return null;
    }

    @Override
    public String removeTablistNames(OnlineMinecraftPlayer onlineMinecraftPlayer, TablistEntry tablistEntry) {
        return null;
    }

}
