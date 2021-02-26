/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 29.12.19, 19:50
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

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.pretronic.libraries.command.sender.CommandSender;
import net.pretronic.libraries.concurrent.TaskScheduler;
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler;
import net.pretronic.libraries.dependency.DependencyManager;
import net.pretronic.libraries.event.EventPriority;
import net.pretronic.libraries.logging.Debug;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.logging.bridge.slf4j.SLF4JStaticBridge;
import net.pretronic.libraries.logging.level.DebugLevel;
import net.pretronic.libraries.logging.level.LogLevel;
import net.pretronic.libraries.message.MessageProvider;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriber;
import net.pretronic.libraries.message.bml.variable.describer.VariableDescriberRegistry;
import net.pretronic.libraries.plugin.description.DefaultPluginDescription;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.plugin.loader.DefaultPluginLoader;
import net.pretronic.libraries.plugin.manager.PluginManager;
import net.pretronic.libraries.plugin.service.ServiceRegistry;
import net.pretronic.libraries.utility.GeneralUtil;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.runtime.api.loader.LoaderConfiguration;
import org.mcnative.runtime.api.utils.Env;
import org.mcnative.runtime.bungeecord.player.BungeeProxiedPlayer;
import org.mcnative.runtime.bungeecord.player.permission.BungeeCordPermissionProvider;
import org.mcnative.runtime.bungeecord.player.permission.BungeeCordPlayerDesign;
import org.mcnative.runtime.bungeecord.plugin.command.McNativeCommand;
import org.mcnative.runtime.bungeecord.server.BungeeCordServerStatusResponse;
import org.mcnative.runtime.bungeecord.server.WrappedBungeeMinecraftServer;
import org.mcnative.runtime.api.*;
import org.mcnative.runtime.api.network.Network;
import org.mcnative.runtime.api.network.component.server.MinecraftServer;
import org.mcnative.runtime.api.network.component.server.ServerStatusResponse;
import org.mcnative.runtime.api.player.PlayerDesign;
import org.mcnative.runtime.api.player.PlayerManager;
import org.mcnative.runtime.api.player.data.PlayerDataProvider;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;
import org.mcnative.runtime.api.plugin.configuration.ConfigurationProvider;
import org.mcnative.runtime.api.serviceprovider.permission.PermissionProvider;
import org.mcnative.runtime.api.serviceprovider.placeholder.PlaceholderProvider;
import org.mcnative.runtime.api.text.format.ColoredString;
import org.mcnative.runtime.common.DefaultLoaderConfiguration;
import org.mcnative.runtime.common.DefaultObjectFactory;
import org.mcnative.runtime.common.player.OfflineMinecraftPlayer;
import org.mcnative.runtime.common.player.data.DefaultPlayerDataProvider;
import org.mcnative.runtime.common.plugin.configuration.DefaultConfigurationProvider;
import org.mcnative.runtime.common.serviceprovider.McNativePlaceholderProvider;
import org.mcnative.runtime.common.serviceprovider.message.DefaultMessageProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class BungeeCordMcNative implements McNative {

    private final PluginVersion apiVersion;
    private final PluginVersion implementationVersion;
    private final MinecraftPlatform platform;
    private final PretronicLogger logger;
    private final TaskScheduler scheduler;
    private final CommandSender consoleSender;
    private final ObjectFactory factory;
    private final LoaderConfiguration loaderConfiguration;
    private final Collection<Env> variables;

    private final PluginManager pluginManager;
    private final DependencyManager dependencyManager;
    private final PlayerManager playerManager;
    private final LocalService local;
    private final McNativeConsoleCredentials consoleCredentials;

    private Network network;
    private boolean ready;

    public BungeeCordMcNative(PluginVersion apiVersion,PluginVersion implVersion,PluginManager pluginManager, PlayerManager playerManager, LocalService local,Collection<Env> variables, McNativeConsoleCredentials consoleCredentials) {
        this.implementationVersion = implVersion;
        this.apiVersion = apiVersion;
        this.platform = new BungeeCordPlatform();

        JdkPretronicLogger logger0 = new JdkPretronicLogger(ProxyServer.getInstance().getLogger());
        if(McNativeBungeeCordConfiguration.DEBUG){
            logger0.getLogLevelTranslation().replace(LogLevel.DEBUG, Level.INFO);
            logger0.setPrefixProcessor(level -> level == LogLevel.DEBUG ? "(Debug) " : null);
        }else{
            logger0.getLogLevelTranslation().replace(LogLevel.DEBUG, Level.FINEST);
        }
        Debug.setLogger(logger0);
        Debug.setDebugLevel(DebugLevel.NORMAL);
        Debug.setLogLevel(LogLevel.DEBUG);
        this.logger = logger0;


        this.scheduler = new SimpleTaskScheduler();
        this.consoleSender = new McNativeCommand.MappedCommandSender(ProxyServer.getInstance().getConsole());
        this.dependencyManager = new DependencyManager(logger,new File("plugins/McNative/lib/dependencies"));
        this.factory = new DefaultObjectFactory();
        this.variables = variables;
        this.consoleCredentials = consoleCredentials;

        this.pluginManager = pluginManager;
        this.playerManager = playerManager;
        this.local = local;

        this.loaderConfiguration = DefaultLoaderConfiguration.load(new File("plugins/McNative/update.yml"));
        SLF4JStaticBridge.trySetLogger(logger);
    }

    @Override
    public PluginVersion getApiVersion() {
        return apiVersion;
    }

    @Override
    public PluginVersion getImplementationVersion() {
        return implementationVersion;
    }

    @Override
    public LoaderConfiguration getRolloutConfiguration() {
        return this.loaderConfiguration;
    }

    @Override
    public McNativeConsoleCredentials getConsoleCredentials() {
        return this.consoleCredentials;
    }

    @Override
    public Collection<Env> getVariables() {
        return variables;
    }

    @Override
    public Env getVariable(String name) {
        Validate.notNull(name);
        return Iterators.findOne(this.variables, env -> env.getName().equalsIgnoreCase(name));
    }

    @Override
    public boolean hasVariable(String name) {
        Validate.notNull(name);
        return getVariable(name) != null;
    }

    @Override
    public void setVariable(String name, Object value) {
        Validate.notNull(name);
        Iterators.remove(this.variables, env -> env.getName().equalsIgnoreCase(name));
        if(value != null) this.variables.add(new Env(name,value));
    }

    @Override
    public MinecraftPlatform getPlatform() {
        return platform;
    }

    @Override
    public PretronicLogger getLogger() {
        return logger;
    }

    @Override
    public ServiceRegistry getRegistry() {
        return pluginManager;
    }

    @Override
    public TaskScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public CommandSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    @Override
    public ObjectFactory getObjectFactory() {
        return factory;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public ExecutorService getExecutorService() {
        return GeneralUtil.getDefaultExecutorService();
    }

    @Override
    public boolean isNetworkAvailable() {
        return true;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(Network network) {
        Validate.notNull(network);
        this.network = network;
    }

    @Override
    public LocalService getLocal() {
        return local;
    }

    @Override
    public void shutdown() {
        ProxyServer.getInstance().stop();
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    protected void setReady(boolean ready) {
        this.ready = ready;
    }

    protected void registerDefaultProviders(){
        pluginManager.registerService(this, ConfigurationProvider.class,new DefaultConfigurationProvider());
        pluginManager.registerService(this, PlayerDataProvider.class,new DefaultPlayerDataProvider());
        pluginManager.registerService(this, MessageProvider.class,new DefaultMessageProvider());
        pluginManager.registerService(this, PermissionProvider.class,new BungeeCordPermissionProvider());
        pluginManager.registerService(this, PlaceholderProvider.class,new McNativePlaceholderProvider(), EventPriority.LOW);
    }

    protected void registerDefaultCommands() {
        getLocal().getCommandManager().registerCommand(new org.mcnative.runtime.common.commands.McNativeCommand(this,"p","proxy"));
    }

    protected void registerDefaultDescribers(){
        VariableDescriberRegistry.registerDescriber(PlayerDesign.class);
        VariableDescriberRegistry.registerDescriber(MinecraftPlugin.class);
        VariableDescriberRegistry.registerDescriber(PluginDescription.class);
        VariableDescriberRegistry.registerDescriber(DefaultPluginDescription.class);
        VariableDescriberRegistry.registerDescriber(DefaultPluginLoader.class);
        VariableDescriberRegistry.registerDescriber(PluginVersion.class);
        VariableDescriberRegistry.registerDescriber(WrappedBungeeMinecraftServer.class);
        VariableDescriberRegistry.registerDescriber(MinecraftServer.class);
        VariableDescriberRegistry.registerDescriber(ProxyServer.class);

        VariableDescriber<?> designDescriber = VariableDescriberRegistry.registerDescriber(BungeeCordPlayerDesign.class);
        ColoredString.makeDescriberColored(designDescriber);

        VariableDescriber<BungeeProxiedPlayer> oPlayerDescriber = VariableDescriberRegistry.registerDescriber(BungeeProxiedPlayer.class);
        ColoredString.makeFunctionColored(oPlayerDescriber,"displayName");

        VariableDescriber<OfflineMinecraftPlayer> playerDescriber = VariableDescriberRegistry.registerDescriber(OfflineMinecraftPlayer.class);
        ColoredString.makeFunctionColored(playerDescriber,"displayName");
    }

    protected void registerDefaultCreators(){
        this.factory.registerCreator(ServerStatusResponse.class, parameters -> new BungeeCordServerStatusResponse(new ServerPing()));

        this.factory.registerCreator(ServerStatusResponse.PlayerInfo.class, parameters -> {
            if(parameters.length == 1)return new BungeeCordServerStatusResponse.DefaultPlayerInfo((String) parameters[0]);
            else return new BungeeCordServerStatusResponse.DefaultPlayerInfo((String) parameters[0],(UUID) parameters[1]);
        });
    }
}
