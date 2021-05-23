/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 25.08.19, 19:22
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

import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import net.pretronic.libraries.command.command.configuration.CommandConfiguration;
import net.pretronic.libraries.command.command.configuration.DefaultCommandConfiguration;
import net.pretronic.libraries.document.DocumentRegistry;
import net.pretronic.libraries.event.DefaultEventBus;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import net.pretronic.libraries.utility.reflect.UnsafeInstanceCreator;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.McNativeConsoleCredentials;
import org.mcnative.runtime.api.connection.MinecraftConnection;
import org.mcnative.runtime.api.event.service.local.LocalServiceShutdownEvent;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.ConnectedMinecraftPlayer;
import org.mcnative.runtime.api.player.chat.ChatChannel;
import org.mcnative.runtime.api.player.chat.GroupChatFormatter;
import org.mcnative.runtime.api.player.tablist.Tablist;
import org.mcnative.runtime.api.player.tablist.TablistEntry;
import org.mcnative.runtime.api.player.tablist.TablistFormatter;
import org.mcnative.runtime.api.player.tablist.TablistOverviewFormatter;
import org.mcnative.runtime.api.proxy.ProxyService;
import org.mcnative.runtime.api.text.components.MessageComponent;
import org.mcnative.runtime.api.text.components.MessageKeyComponent;
import org.mcnative.runtime.api.text.components.TargetMessageKeyComponent;
import org.mcnative.runtime.api.utils.Env;
import org.mcnative.runtime.bungeecord.event.McNativeBridgeEventHandler;
import org.mcnative.runtime.bungeecord.network.McNativeGlobalActionListener;
import org.mcnative.runtime.bungeecord.network.McNativePlayerActionListener;
import org.mcnative.runtime.bungeecord.network.bungeecord.BungeecordProxyNetwork;
import org.mcnative.runtime.bungeecord.network.cloudnet.CloudNetV2PlatformListener;
import org.mcnative.runtime.bungeecord.network.cloudnet.CloudNetV3PlatformListener;
import org.mcnative.runtime.bungeecord.player.BungeeCordPlayerManager;
import org.mcnative.runtime.bungeecord.player.BungeeTablist;
import org.mcnative.runtime.bungeecord.plugin.BungeeCordPluginManager;
import org.mcnative.runtime.bungeecord.plugin.McNativeBungeeEventBus;
import org.mcnative.runtime.bungeecord.plugin.command.BungeeCordCommandManager;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerMap;
import org.mcnative.runtime.bungeecord.shared.McNativeBridgedEventBus;
import org.mcnative.runtime.bungeecord.waterfall.McNativeWaterfallEventBus;
import org.mcnative.runtime.client.integrations.ClientIntegration;
import org.mcnative.runtime.common.event.service.local.DefaultLocalServiceShutdownEvent;
import org.mcnative.runtime.common.maf.MAFService;
import org.mcnative.runtime.common.network.event.NetworkEventHandler;
import org.mcnative.runtime.common.protocol.DefaultPacketManager;
import org.mcnative.runtime.common.serviceprovider.message.ResourceMessageExtractor;
import org.mcnative.runtime.network.integrations.cloudnet.v2.CloudNetV2Network;
import org.mcnative.runtime.network.integrations.cloudnet.v3.CloudNetV3Network;
import org.mcnative.runtime.protocol.java.MinecraftJavaProtocol;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class McNativeLauncher {

    private static Plugin PLUGIN;

    public static Plugin getPlugin() {
        return PLUGIN;
    }

    public static void launchMcNative(){
        launchMcNative(new HashMap<>());
    }

    public static void launchMcNative(Map<String,String> variables0){
        Collection<Env> variables = Iterators.map(variables0.entrySet(), entry -> new Env(entry.getKey().toLowerCase(),entry.getValue()));
        launchMcNativeInternal(variables,setupDummyPlugin());
    }

    public static void launchMcNativeInternal(Collection<Env> variables,Plugin plugin){
        if(McNative.isAvailable()) return;
        PluginVersion apiVersion = PluginVersion.parse(McNativeLauncher.class.getPackage().getSpecificationVersion());
        PluginVersion implementationVersion = PluginVersion.ofImplementation(McNativeLauncher.class);
        PLUGIN = plugin;
        Logger logger = ProxyServer.getInstance().getLogger();
        logger.info(McNative.CONSOLE_PREFIX+"McNative is starting, please wait...");
        logger.info(McNative.CONSOLE_PREFIX+"Api Version: "+apiVersion.getName());
        logger.info(McNative.CONSOLE_PREFIX+"Impl Version: "+implementationVersion.getName());
        ProxyServer proxy = ProxyServer.getInstance();

        DocumentRegistry.getDefaultContext().registerMappingAdapter(CommandConfiguration.class, DefaultCommandConfiguration.class);

        if(!McNativeBungeeCordConfiguration.load(new JdkPretronicLogger(logger),new File("plugins/McNative/"))) return;

        BungeeCordServerMap serverMap = new BungeeCordServerMap();
        serverMap.inject();
        logger.info(McNative.CONSOLE_PREFIX+"McNative initialised and injected server map.");

        BungeeCordPluginManager pluginManager = new BungeeCordPluginManager();
        logger.info(McNative.CONSOLE_PREFIX+"McNative initialised plugin manager.");

        BungeeCordPlayerManager playerManager = new BungeeCordPlayerManager();
        logger.info(McNative.CONSOLE_PREFIX+"McNative initialised player manager.");

        BungeeCordCommandManager commandManager = new BungeeCordCommandManager(pluginManager,ProxyServer.getInstance().getPluginManager());
        logger.info(McNative.CONSOLE_PREFIX+"McNative initialised and injected command manager.");

        DefaultEventBus mcnativeEventbus = new DefaultEventBus(new NetworkEventHandler());

        BungeeCordService localService = new BungeeCordService(new DefaultPacketManager()
                ,commandManager,playerManager,mcnativeEventbus,serverMap);

        McNativeConsoleCredentials credentials = setupCredentials(variables);
        BungeeCordMcNative instance = new BungeeCordMcNative(apiVersion,implementationVersion
                ,pluginManager,playerManager, localService,variables,credentials);
        //mcnativeEventbus.setInjector(instance.getInjector());

        McNative.setInstance(instance);
        instance.setNetwork(setupNetwork(logger,localService,instance.getExecutorService(),serverMap));

        MinecraftJavaProtocol.register(localService.getPacketManager());
        ClientIntegration.register();

        instance.getNetwork().getMessenger().registerChannel("mcnative_player",ObjectOwner.SYSTEM,new McNativePlayerActionListener());
        instance.getNetwork().getMessenger().registerChannel("mcnative_global",ObjectOwner.SYSTEM,new McNativeGlobalActionListener());

        instance.registerDefaultProviders();
        instance.registerDefaultCommands();
        instance.registerDefaultDescribers();
        instance.registerDefaultCreators();
        instance.registerSingletons();

        proxy.setConfigurationAdapter(new McNativeConfigurationAdapter(proxy.getConfigurationAdapter()));
        logger.info(McNative.CONSOLE_PREFIX+"McNative has overwritten the configuration adapter.");

        McNativeBridgedEventBus eventBus;
        if(isWaterfallBase()){
            eventBus = new McNativeWaterfallEventBus(localService.getEventBus());
        }else{
            eventBus = new McNativeBungeeEventBus(localService.getEventBus());
        }
        logger.info(McNative.CONSOLE_PREFIX+"McNative initialised and injected event bus.");

        new McNativeBridgeEventHandler(eventBus,localService.getEventBus(),playerManager,serverMap);
        logger.info(McNative.CONSOLE_PREFIX+"McNative has overwritten default bungeecord events.");

        McNativeBungeeCordConfiguration.postLoad();
        setupConfiguredServices();

        instance.setReady(true);

        ResourceMessageExtractor.extractMessages(McNativeLauncher.class.getClassLoader(),"system-messages/","McNative");

        if(McNativeBungeeCordConfiguration.CONSOLE_MAF_ENABLED && McNative.getInstance().getConsoleCredentials() != null){
            MAFService.start();
        }

        logger.info(McNative.CONSOLE_PREFIX+"McNative successfully started.");

    }

    private static boolean isWaterfallBase(){
        return ProxyServer.getInstance().getVersion().toLowerCase().contains("waterfall")
                || ProxyServer.getInstance().getVersion().toLowerCase().contains("travertine")
                || ProxyServer.getInstance().getName().equalsIgnoreCase("FlameCord");
    }

    private static McNativeConsoleCredentials setupCredentials(Collection<Env> variables){
        Env networkId = Iterators.findOne(variables, env -> env.getName().equalsIgnoreCase("mcnative.networkId"));
        Env secret = Iterators.findOne(variables, env -> env.getName().equalsIgnoreCase("mcnative.secret"));
        if(networkId != null && secret != null){
            return new McNativeConsoleCredentials(networkId.getValue().toString(),secret.getValue().toString());
        }else if(!McNativeBungeeCordConfiguration.CONSOLE_NETWORK_ID.equals("00000-00000-00000")){
            return new McNativeConsoleCredentials(McNativeBungeeCordConfiguration.CONSOLE_NETWORK_ID,McNativeBungeeCordConfiguration.CONSOLE_SECRET);
        }
        return null;
    }

    private static Network setupNetwork(Logger logger, ProxyService proxy, ExecutorService executor, BungeeCordServerMap serverMap){
        if(ProxyServer.getInstance().getPluginManager().getPlugin("CloudNetAPI") != null){
            logger.info(McNative.CONSOLE_PREFIX+"(Network) Initialized CloudNet V2 networking technology");
            CloudNetV2Network network = new CloudNetV2Network(executor);
            new CloudNetV2PlatformListener(network.getMessenger());
            return network;
        }else if(ProxyServer.getInstance().getPluginManager().getPlugin("CloudNet-Bridge") != null){
            logger.info(McNative.CONSOLE_PREFIX+"(Network) Initialized CloudNet V3 networking technology");
            CloudNetV3Network network = new CloudNetV3Network(executor);
            new CloudNetV3PlatformListener(network.getMessenger());
            return network;
        }else{
            logger.info(McNative.CONSOLE_PREFIX+"(Network) Initialized BungeeCord networking technology");
            return new BungeecordProxyNetwork(proxy,executor,serverMap);
        }
    }

    protected static void shutdown(){
        if(!McNative.isAvailable()) return;
        Logger logger = ProxyServer.getInstance().getLogger();
        logger.info(McNative.CONSOLE_PREFIX+"McNative is stopping, please wait...");
        McNative instance = McNative.getInstance();

        if(instance != null){
            instance.getLocal().getEventBus().callEvent(LocalServiceShutdownEvent.class,new DefaultLocalServiceShutdownEvent());

            instance.getLogger().shutdown();
            instance.getScheduler().shutdown();
            instance.getExecutorService().shutdown();
            instance.getPluginManager().shutdown();
            ((BungeeCordMcNative)instance).setReady(false);
        }
    }

    private static void setupConfiguredServices() {
        McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM,new LabyModListener());

        if (McNativeBungeeCordConfiguration.PLAYER_GLOBAL_CHAT_ENABLED) {
            ChatChannel serverChat = ChatChannel.newChatChannel();
            serverChat.setName("ServerChat");
            serverChat.setMessageFormatter((GroupChatFormatter) (sender, variables, message) -> McNativeBungeeCordConfiguration.PLAYER_GLOBAL_CHAT);
            McNative.getInstance().getLocal().setServerChat(serverChat);
        }

        //Motd setup
        File serverIcon = new File("server-icon.png");
        if (!serverIcon.exists()) {
            try {
                McNativeBridgeEventHandler.DEFAULT_FAVICON = Favicon.create(ImageIO.read(ServerStatusResponse.DEFAULT_FAVICON_URL));
            } catch (Exception ignored) {
            }
        }

        if (McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_ENABLED) {
            Tablist tablist = new BungeeTablist();
            tablist.setFormatter(new TablistFormatter() {
                @Override
                public MessageComponent<?> formatPrefix(ConnectedMinecraftPlayer player, TablistEntry entry, VariableSet variables) {
                    if (entry instanceof MinecraftConnection) {
                        return new TargetMessageKeyComponent((MinecraftConnection) entry, McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_PREFIX_LOADED);
                    } else {
                        return new MessageKeyComponent(McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_PREFIX_LOADED);
                    }
                }

                @Override
                public MessageComponent<?> formatSuffix(ConnectedMinecraftPlayer player, TablistEntry entry, VariableSet variables) {
                    if (entry instanceof MinecraftConnection) {
                        return new TargetMessageKeyComponent((MinecraftConnection) entry, McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_SUFFIX_LOADED);
                    } else {
                        return new MessageKeyComponent(McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_SUFFIX_LOADED);
                    }
                }
            });

            if (McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_OVERVIEW_ENABLED) {
                tablist.setOverviewFormatter(new TablistOverviewFormatter() {

                    @Override
                    public MessageComponent<?> formatHeader(ConnectedMinecraftPlayer receiver, VariableSet headerVariables, VariableSet footerVariables) {
                        return McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_OVERVIEW_HEADER_LOADED;
                    }

                    @Override
                    public MessageComponent<?> formatFooter(ConnectedMinecraftPlayer receiver, VariableSet headerVariables, VariableSet footerVariables) {
                        return McNativeBungeeCordConfiguration.PLAYER_GLOBAL_TABLIST_OVERVIEW_FOOTER_LOADED;
                    }
                });
            }

            McNative.getInstance().getLocal().setServerTablist(tablist);
            McNative.getInstance().getLocal().getEventBus().subscribe(ObjectOwner.SYSTEM, tablist);
        }
    }

    @SuppressWarnings("unchecked")
    private static Plugin setupDummyPlugin(){
        PluginDescription description = new PluginDescription();
        description.setName("McNative");
        description.setVersion(McNativeLauncher.class.getPackage().getImplementationVersion());
        description.setAuthor("Pretronic and McNative contributors");
        description.setMain("reflected");

        Plugin plugin = UnsafeInstanceCreator.newInstance(DummyPlugin.class);
        ReflectionUtil.invokeMethod(Plugin.class,plugin,"init"
                ,new Class[]{ProxyServer.class,PluginDescription.class}
                ,new Object[]{ProxyServer.getInstance(),description});

        Map<String, Plugin> plugins = ReflectionUtil.getFieldValue(PluginManager.class,ProxyServer.getInstance().getPluginManager(),"plugins", Map.class);
        plugins.put(description.getName(),plugin);
        return plugin;
    }

    public static class DummyPlugin extends Plugin{

        @Override
        public void onDisable() {
            McNativeLauncher.shutdown();
        }
    }

}
