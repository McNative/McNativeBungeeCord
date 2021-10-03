package org.mcnative.runtime.bungeecord;

import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.protocol.packet.TabCompleteRequest;

public class Test implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void request(TabCompleteEvent event){
        System.out.println("REQUEST-EVENT: "+event.getCursor()+" | "+event.getSuggestions()+" | "+event.isCancelled());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void request(TabCompleteResponseEvent event){
        System.out.println("RESPONSE-EVENT: "+event.getSuggestions()+" | "+event.isCancelled());
    }

}
