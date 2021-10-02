package org.mcnative.runtime.bungeecord;

import net.pretronic.libraries.event.EventPriority;
import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteEvent;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;
import org.mcnative.runtime.api.protocol.packet.type.player.complete.MinecraftPlayerTabCompleteResponsePacket;

import java.util.List;

public class TabCompleteInjectListener {

    @Listener(priority = EventPriority.LOW)
    public void handleTabComplete(MinecraftPlayerTabCompleteEvent event){
        System.out.println("Request: "+event.getCursor());
    }

    @Listener(priority = EventPriority.LOW)
    public void handleTabComplete(MinecraftPlayerTabCompleteResponseEvent event){
        System.out.println("Response: "+event.getCursor());
        if(event.getCursor() == null)return;
        String cursor = (event.getCursor().length() > 0 && event.getCursor().charAt(0) == '/' ? event.getCursor().substring(1) : event.getCursor()).toLowerCase();
        List<String> completion = Iterators.map(McNative.getInstance().getLocal().getCommandManager().getCommands()
                , command -> command.getConfiguration().getName()
                , command -> {
            String name = command.getConfiguration().getName().toLowerCase();
            return name.startsWith(cursor) && !event.getSuggestions().contains(name);
        });
        event.getSuggestions().addAll(completion);
        System.out.println("Response: "+event.getSuggestions());
    }

}
