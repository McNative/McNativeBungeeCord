package org.mcnative.runtime.bungeecord;

import net.pretronic.libraries.event.EventPriority;
import net.pretronic.libraries.event.Listener;
import net.pretronic.libraries.utility.Iterators;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.player.MinecraftPlayerTabCompleteResponseEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TabCompleteInjectListener {

    @Listener(priority = EventPriority.LOW)
    public void handleTabComplete(MinecraftPlayerTabCompleteResponseEvent event){
        if(event.getCursor() != null && event.getCursor().charAt(0) == '/' && event.getCursor().indexOf(' ') < 0){
            String cursor = event.getCursor().substring(1).toLowerCase().trim();
            List<String> completion = Iterators.map(McNative.getInstance().getLocal().getCommandManager().getCommands()
                    , command -> command.getConfiguration().getName().toLowerCase()
                    , command -> {
                        if(command.getConfiguration().getPermission() == null || event.getPlayer().hasPermission(command.getConfiguration().getPermission())){
                            String name = command.getConfiguration().getName().toLowerCase();
                            return name.startsWith(cursor);
                        }
                        return false;
                    });

            HashSet<String> result = new HashSet<>(event.getSuggestions());
            result.addAll(completion);


            event.setSuggestions(new ArrayList<>(result));
        }
    }

}
