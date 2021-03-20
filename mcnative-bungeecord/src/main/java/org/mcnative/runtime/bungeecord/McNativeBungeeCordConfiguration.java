/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 06.04.20, 11:35
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

package org.mcnative.runtime.bungeecord;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.annotations.DocumentIgnored;
import net.pretronic.libraries.document.annotations.DocumentKey;
import net.pretronic.libraries.document.annotations.DocumentRequired;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.message.MessageProvider;
import net.pretronic.libraries.message.bml.Message;
import net.pretronic.libraries.message.bml.parser.MessageParser;
import net.pretronic.libraries.utility.map.Pair;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.network.component.server.MinecraftServerType;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.MessageKeyComponent;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.common.plugin.configuration.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class McNativeBungeeCordConfiguration {

    public static transient DocumentFileType FORMAT = DocumentFileType.YAML;

    @DocumentKey("debug")
    public static boolean DEBUG = false;

    @DocumentKey("console.networkId")
    public static String CONSOLE_NETWORK_ID = "00000-00000-00000";

    @DocumentKey("console.secret")
    public static String CONSOLE_SECRET = "00000-00000-00000";

    @DocumentKey("console.mafEnabled")
    public static boolean CONSOLE_MAF_ENABLED = true;

    @DocumentIgnored
    public static Map<String,ConfiguredServer> SERVER_SERVERS = new HashMap<>();

    @DocumentKey("player.displayName.format")
    public static String PLAYER_DISPLAY_NAME_FORMAT = "{design.color}{name}";

    public static Map<String,String> PLAYER_COLORS_COLORS = new LinkedHashMap<>();
    public static String PLAYER_COLORS_DEFAULT = "&7";

    public static boolean PLAYER_GLOBAL_CHAT_ENABLED = false;
    public static String PLAYER_GLOBAL_CHAT_FORMAT = "&8[{player.server}&8] &e{design.chat}{player.name}&8:&f {message}";

    public static boolean PLAYER_GLOBAL_TABLIST_ENABLED = false;

    public static String PLAYER_GLOBAL_TABLIST_PREFIX = "{design.prefix}";
    public static String PLAYER_GLOBAL_TABLIST_SUFFIX = "{design.suffix}";

    public static boolean PLAYER_GLOBAL_TABLIST_OVERVIEW_ENABLED = false;
    public static String PLAYER_GLOBAL_TABLIST_OVERVIEW_HEADER = "&4Header";
    public static String PLAYER_GLOBAL_TABLIST_OVERVIEW_FOOTER = "&8Footer";

    public static transient Message PLAYER_GLOBAL_TABLIST_PREFIX_LOADED;
    public static transient Message PLAYER_GLOBAL_TABLIST_SUFFIX_LOADED;
    public static transient MessageComponent<?> PLAYER_GLOBAL_CHAT;
    public static transient MessageComponent<?> PLAYER_GLOBAL_TABLIST_OVERVIEW_HEADER_LOADED;
    public static transient MessageComponent<?> PLAYER_GLOBAL_TABLIST_OVERVIEW_FOOTER_LOADED;

    @DocumentKey("network.messaging.packetManipulation.upstream")
    public static boolean NETWORK_PACKET_MANIPULATION_UPSTREAM_ENABLED = true;
    @DocumentKey("network.messaging.packetManipulation.downstream")
    public static boolean NETWORK_PACKET_MANIPULATION_DOWNSTREAM_ENABLED = false;

    static{
        PLAYER_COLORS_COLORS.put("mcnative.player.color.administrator","&4");
        PLAYER_COLORS_COLORS.put("mcnative.player.color.moderator","&c");
        PLAYER_COLORS_COLORS.put("mcnative.player.color.premium","&6");
    }

    public static boolean load(PretronicLogger logger, File location){
        OfflineMinecraftPlayer.DISPLAY_NAME_FORMAT = PLAYER_DISPLAY_NAME_FORMAT;

        logger.info(McNative.CONSOLE_PREFIX+"Searching configuration file");
        Pair<File,DocumentFileType> configSpec = Document.findExistingType(location,"config");
        File configFile;
        DocumentFileType type;

        if(configSpec == null){
            configFile = new File(location,"config.yml");
            type = DocumentFileType.YAML;
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException exception) {
                logger.error(McNative.CONSOLE_PREFIX+"Could not create configuration file (config.yml)",exception);
                exception.printStackTrace();
                return false;
            }
        }else{
            configFile = configSpec.getKey();
            type = configSpec.getValue();
        }
        logger.info(McNative.CONSOLE_PREFIX+"Loading configuration (config.{})",type.getEnding());

        try{
            FORMAT = type;
            FileConfiguration.FILE_TYPE = type;
            Document config = type.getReader().read(configFile);
            Document.loadConfigurationClass(McNativeBungeeCordConfiguration.class,config);
            type.getWriter().write(configFile,config);
        }catch (Exception exception){
            logger.info(McNative.CONSOLE_PREFIX+"Could not load configuration (config."+type.getEnding()+")",exception);
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static void postLoad(){
        PLAYER_GLOBAL_TABLIST_PREFIX_LOADED = parseCustomMessage(PLAYER_GLOBAL_TABLIST_PREFIX);
        PLAYER_GLOBAL_TABLIST_SUFFIX_LOADED = parseCustomMessage(PLAYER_GLOBAL_TABLIST_SUFFIX);

        PLAYER_GLOBAL_TABLIST_OVERVIEW_HEADER_LOADED = new MessageKeyComponent(parseCustomMessage(PLAYER_GLOBAL_TABLIST_OVERVIEW_HEADER));
        PLAYER_GLOBAL_TABLIST_OVERVIEW_FOOTER_LOADED = new MessageKeyComponent(parseCustomMessage(PLAYER_GLOBAL_TABLIST_OVERVIEW_FOOTER));
        PLAYER_GLOBAL_CHAT = new MessageKeyComponent(parseCustomMessage(PLAYER_GLOBAL_CHAT_FORMAT));
    }

    private static Message parseCustomMessage(String input){
        return new MessageParser(McNative.getInstance().getRegistry()
                .getService(MessageProvider.class).getProcessor(),input).parse();
    }

    public static class ConfiguredServer {

        @DocumentRequired
        private final InetSocketAddress address;
        private final String permission;
        private final MinecraftServerType type;

        public ConfiguredServer(InetSocketAddress address, String permission, MinecraftServerType type) {
            this.address = address;
            this.permission = permission;
            this.type = type;
        }

        public InetSocketAddress getAddress() {
            return address;
        }

        public String getPermission() {
            return permission;
        }

        public MinecraftServerType getType() {
            return type;
        }
    }



}
