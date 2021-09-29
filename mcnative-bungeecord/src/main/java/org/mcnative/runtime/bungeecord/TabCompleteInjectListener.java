package org.mcnative.runtime.bungeecord;

import net.pretronic.libraries.event.EventPriority;
import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;

import java.util.List;

public class TabCompleteInjectListener {

    @Listener(priority = EventPriority.LOW)
    public void handleTabComplete(MinecraftPlayerTabCompleteResponseEvent event){
        System.out.println("TAB COMPLETE "+event.getCursor());
        List<String> completion = Iterators.map(McNative.getInstance().getLocal().getCommandManager().getCommands()
                , command -> command.getConfiguration().getName()
                , command -> command.getConfiguration().getName().startsWith(event.getCursor()));
        event.getSuggestions().addAll(completion);
    }

}
