package org.mcnative.runtime.bungeecord;

import net.pretronic.libraries.event.Listener;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerCustomClientLoginEvent;
import org.mcnative.runtime.api.player.client.LabyModClient;

public class LabyModListener {

    @Listener
    public void onLabyModPlayerLogin(MinecraftPlayerCustomClientLoginEvent event){
        if(event.getClient().getName().equalsIgnoreCase("LabyMod")){
            LabyModClient client = (LabyModClient) event.getClient();
            if(McNativeBungeeCordConfiguration.LABYMOD_BANNER_ENABLED) client.sendServerBanner(McNativeBungeeCordConfiguration.LABYMOD_BANNER_URL);
            if(McNativeBungeeCordConfiguration.LABYMOD_WATERMARK_ENABLED) client.sendWatermark(true);
            if(!McNativeBungeeCordConfiguration.LABYMOD_VOICECHAT_ENABLED) client.disableVoiceChat();

            if(McNativeBungeeCordConfiguration.LABYMOD_ALERT_ENABLED) {
                client.sendCurrentGameModeInfo(McNativeBungeeCordConfiguration.LABYMOD_ALERT_GAMEMODE);
            }
        }
    }

}
