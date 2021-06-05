/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 25.11.19, 20:24
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

package org.mcnative.runtime.bungeecord.plugin;

import net.md_5.bungee.api.ProxyServer;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.lifecycle.LifecycleState;
import net.pretronic.libraries.plugin.loader.PluginLoader;
import net.pretronic.libraries.plugin.manager.PluginManager;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.interfaces.OwnerUnregisterAble;
import net.pretronic.libraries.utility.map.callback.CallbackMap;
import net.pretronic.libraries.utility.map.callback.LinkedHashCallbackMap;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.event.service.registry.ServiceRegisterRegistryEvent;
import org.mcnative.runtime.api.event.service.registry.ServiceUnregisterRegistryEvent;
import org.mcnative.runtime.common.McNativeMappingException;
import org.mcnative.runtime.common.event.service.registry.DefaultServiceRegisterRegistryEvent;
import org.mcnative.runtime.common.event.service.registry.DefaultServiceUnregisterRegistryEvent;
import org.mcnative.runtime.common.serviceprovider.message.ResourceMessageExtractor;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BungeeCordPluginManager implements PluginManager {

    private final Collection<ServiceEntry> services;
    private final Map<String, BiConsumer<Plugin<?>,LifecycleState>> stateListeners;
    private final Collection<PluginLoader> loaders;
    private final Collection<Plugin<?>> plugins;

    private final net.md_5.bungee.api.plugin.PluginManager original;

    public BungeeCordPluginManager() {
        this.original = ProxyServer.getInstance().getPluginManager();
        this.services = new ArrayList<>();
        this.stateListeners = new LinkedHashMap<>();
        this.loaders = new ArrayList<>();
        this.plugins = new ArrayList<>();
        inject();
    }

    @Override
    public PretronicLogger getLogger() {
        return McNative.getInstance().getLogger();
    }

    @Override
    public Collection<Plugin<?>> getPlugins() {
        return plugins;
    }

    @Override
    public Plugin<?> getPlugin(String name) {
        return Iterators.findOne(this.plugins, plugin -> plugin.getDescription().getName().equals(name));
    }

    @Override
    public Plugin<?> getPlugin(UUID id) {
        return Iterators.findOne(this.plugins, plugin -> plugin.getDescription().getId().equals(id));
    }

    @Override
    public boolean isPluginEnabled(String name) {
        Plugin<?> plugin =  getPlugin(name);
        return plugin != null && plugin.getLoader().isEnabled();
    }

    @Override
    public Collection<PluginLoader> getLoaders() {
        return loaders;
    }

    @Override
    public PluginLoader createPluginLoader(String s) {
        throw new UnsupportedOperationException("BungeeCord bridge is not able to create plugin loaders");
    }

    @Override
    public PluginLoader createPluginLoader(File file) {
        return createPluginLoader(file,null);
    }

    @Override
    public PluginLoader createPluginLoader(File location, PluginDescription description) {
        throw new UnsupportedOperationException("BungeeCord bridge is not able to create plugin loaders");
    }

    @Override
    public PluginDescription detectPluginDescription(File file) {
        throw new UnsupportedOperationException("BungeeCord bridge is not able to detect plugin descriptions");
    }

    @Override
    public Collection<PluginDescription> detectPluginDescriptions(File directory) {
        throw new UnsupportedOperationException("BungeeCord bridge is not able to detect plugin descriptions");
    }

    //Only for McNative Plugins
    @Override
    public void setLifecycleStateListener(String s, BiConsumer<Plugin<?>, LifecycleState> biConsumer) {
        this.stateListeners.put(s,biConsumer);
    }

    @Internal
    @Override
    public void executeLifecycleStateListener(String state, LifecycleState stateEvent, Plugin plugin) {
        if(state.equals(LifecycleState.CONSTRUCTION)) this.plugins.add(plugin);
        else if(state.equals(LifecycleState.INITIALISATION)) ResourceMessageExtractor.extractMessages(plugin);
        else if(state.equals(LifecycleState.UNLOAD)) this.plugins.remove(plugin);

        BiConsumer<Plugin<?>,LifecycleState> listener = this.stateListeners.get(state);
        if(listener != null) listener.accept(plugin,stateEvent);
    }

    @Override
    public Collection<Plugin<?>> enablePlugins(File file) {
        throw new UnsupportedOperationException("BungeeCord bridge is not able to enable plugins");
    }

    @Override
    public void disablePlugins() {
        throw new UnsupportedOperationException("BungeeCord does not support disabling plugins");
    }


    @Override
    public Collection<Class<?>> getAvailableServices() {
        Collection<Class<?>> classes = new HashSet<>();
        services.forEach(serviceEntry -> classes.add(serviceEntry.serviceClass));
        return classes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getServices(Class<T> serviceClass) {
        List<T> services =  Iterators.map(this.services, entry -> (T) entry.service, entry -> entry.serviceClass.equals(serviceClass));
        if(services.isEmpty()) throw new UnsupportedOperationException("Service is not available.");
        return services;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> serviceClass) {
        T result = getServiceOrDefault(serviceClass);
        if(result == null) throw new IllegalArgumentException("Service "+serviceClass+" is not available.");
        return result;
    }

    @Override
    public <T> T getServiceOrDefault(Class<T> serviceClass) {
        return getServiceOrDefault(serviceClass,null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getServiceOrDefault(Class<T> serviceClass, Supplier<T> supplier) {
        List<ServiceEntry> services = Iterators.filter(this.services, entry -> entry.serviceClass.equals(serviceClass));
        services.sort((o1, o2) -> Integer.compare(o2.priority,o1.priority));
        if(services.size() > 0) return  (T) services.get(0).service;
        else if(supplier != null) return supplier.get();
        return null;
    }

    @Override
    public <T> void registerService(ObjectOwner owner, Class<T> serviceClass, T service, byte priority) {
        this.services.add(new ServiceEntry(owner,serviceClass,service,priority));
        McNative.getInstance().getLocal().getEventBus().callEvent(ServiceRegisterRegistryEvent.class
                ,new DefaultServiceRegisterRegistryEvent(serviceClass,service, owner, priority));
    }

    @Override
    public <T> boolean isServiceAvailable(Class<T> serviceClass) {
        for (ServiceEntry entry : this.services) if(entry.serviceClass.equals(serviceClass)) return true;
        return false;
    }

    @Override
    public void unregisterService(Object service) {
        ServiceEntry result = Iterators.removeOne(this.services, entry -> entry.service.equals(service));
        if(result != null) {
            McNative.getInstance().getLocal().getEventBus().callEvent(ServiceUnregisterRegistryEvent.class
                    ,new DefaultServiceUnregisterRegistryEvent(result.serviceClass,result.service, result.owner));
        }
    }

    @Override
    public void unregisterServices(Class<?> serviceClass) {
        ServiceEntry result = Iterators.removeOne(this.services, entry -> entry.serviceClass.equals(serviceClass));
        if(result != null) {
            McNative.getInstance().getLocal().getEventBus().callEvent(ServiceUnregisterRegistryEvent.class
                    ,new DefaultServiceUnregisterRegistryEvent(result.serviceClass,result.service, result.owner));
        }
    }

    @Override
    public void unregisterServices(ObjectOwner owner) {
        List<ServiceEntry> results = Iterators.remove(this.services, entry -> entry.owner.equals(owner));
        for (ServiceEntry result : results) {
            McNative.getInstance().getLocal().getEventBus().callEvent(ServiceUnregisterRegistryEvent.class
                    ,new DefaultServiceUnregisterRegistryEvent(result.serviceClass,result.service, result.owner));
        }
        for (ServiceEntry service : services) {
            if(service.service instanceof OwnerUnregisterAble){
                ((OwnerUnregisterAble) service.service).unregister(owner);
            }
        }
    }

    @Override
    public void shutdown() {
        //Unused
    }

    @Override
    public void provideLoader(PluginLoader loader) {
        if(loaders.contains(loader)) throw new IllegalArgumentException("Loader is already registered.");
        this.loaders.add(loader);
        if(loader.isInstanceAvailable()) {
            System.out.println(loader.getDescription().getName()+" | "+loader.getDescription().getId());
            Iterators.removeOne(this.plugins, plugin -> plugin.getName().equals(loader.getDescription().getName()));
            this.plugins.add(loader.getInstance());
        }
    }

    @Internal
    public net.md_5.bungee.api.plugin.Plugin getMappedPlugin(Plugin<?> original){
        Validate.notNull(original);
        for (net.md_5.bungee.api.plugin.Plugin plugin : this.original.getPlugins()){
            if(plugin.equals(original)) return plugin;
        }
        throw new McNativeMappingException("Plugin "+original.getName()+" is not registered on BungeeCord side");
    }

    @Internal
    public Plugin<?> getMappedPlugin(net.md_5.bungee.api.plugin.Plugin original){
        Validate.notNull(original);
        for (Plugin<?> plugin : plugins) if(plugin.equals(original)) return plugin;
        throw new McNativeMappingException("Plugin "+original.getDescription().getName()+" is not registered on McNative side");
    }

    @Internal
    @SuppressWarnings("unchecked")
    private void inject(){
        Map<String, net.md_5.bungee.api.plugin.Plugin> oldMap = ReflectionUtil.getFieldValue(original,"plugins",Map.class);

        CallbackMap<String, net.md_5.bungee.api.plugin.Plugin> newMap = new LinkedHashCallbackMap<>();
        newMap.setPutCallback((s, plugin) ->{
            if(Iterators.findOne(this.plugins, o -> o.getName().equals(plugin.getDescription().getName())) == null){
                plugins.add(new MappedPlugin(plugin));
            }
        });
        newMap.setRemoveCallback((s, plugin) -> plugins.remove(plugin));

        ReflectionUtil.changeFieldValue(original,"plugins",newMap);
        newMap.putAll(oldMap);
    }

    private static class ServiceEntry {

        private final ObjectOwner owner;
        private final Class<?> serviceClass;
        private final Object service;
        private final byte priority;

        private ServiceEntry(ObjectOwner owner, Class<?> serviceClass, Object service,byte priority) {
            this.owner = owner;
            this.serviceClass = serviceClass;
            this.service = service;
            this.priority = priority;
        }
    }
}
