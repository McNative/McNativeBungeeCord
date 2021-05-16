/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.03.20, 17:57
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

package org.mcnative.runtime.bungeecord.player.permission;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.bungeecord.McNativeBungeeCordConfiguration;
import org.mcnative.runtime.bungeecord.player.BungeeProxiedPlayer;

import java.util.Map;

public class BungeeCordPlayerDesign implements PlayerDesign {

    private final BungeeProxiedPlayer player;

    public BungeeCordPlayerDesign(BungeeProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public String getColor() {
        for (Map.Entry<String, String> entry : McNativeBungeeCordConfiguration.PLAYER_COLORS_COLORS.entrySet()) {
            if(player.getOriginal().hasPermission(entry.getKey())){
                return entry.getValue();
            }
        }
        return McNativeBungeeCordConfiguration.PLAYER_COLORS_DEFAULT;
    }

    @Override
    public String getPrefix() {
        return "";//Unsupported without exception
    }

    @Override
    public String getSuffix() {
        return "";//Unsupported without exception
    }

    @Override
    public String getChat() {
        return "";//Unsupported without exception
    }

    @Override
    public int getPriority() {
        return 0;//Unsupported without exception
    }

    @Override
    public String toJson() {
        Document result = Document.newDocument();
        result.set("color",getColor());
        result.set("prefix","").set("suffix","").set("chat","").set("priority",0);
        return DocumentFileType.JSON.getWriter().write(result,false);
    }
}
