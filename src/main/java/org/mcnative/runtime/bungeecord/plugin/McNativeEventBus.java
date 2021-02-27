/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 24.02.20, 19:40
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

import com.google.common.collect.Multimap;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventHandler;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.event.execution.ExecutionType;
import net.pretronic.libraries.event.executor.MethodEventExecutor;
import net.pretronic.libraries.utility.annonations.Internal;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class McNativeEventBus extends net.md_5.bungee.event.EventBus {

    private final PluginManager original;
    private final EventBus eventBus;
    private final Map<Class<?>, Consumer<?>> mangedEvents;

    private Multimap<Plugin, Listener> listenersByPlugin;

    public McNativeEventBus(EventBus eventBus) {
        super(ProxyServer.getInstance().getLogger());
        this.eventBus = eventBus;
        this.original = ProxyServer.getInstance().getPluginManager();
        this.mangedEvents = new LinkedHashMap<>();
        inject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void post(Object event) {
        Consumer<Object> manager = (Consumer<Object>) mangedEvents.get(event.getClass());
        if(manager != null){
            manager.accept(event);
        }else{
            eventBus.callEvent(event);
        }
    }

    @Override
    public void register(Object listener) {
        for(Method method : listener.getClass().getDeclaredMethods()){
            try{
                EventHandler handler = method.getAnnotation(EventHandler.class);
                if(handler != null && method.getParameterTypes().length == 1){
                    Class<?> eventClass = method.getParameterTypes()[0];
                    Class<?> mappedClass = eventBus.getMappedClass(eventClass);
                    if(mappedClass == null) mappedClass = eventClass;
                    eventBus.addExecutor(mappedClass,new MethodEventExecutor(ObjectOwner.SYSTEM,handler.priority(), ExecutionType.BLOCKING,listener,eventClass,method));
                }
            }catch (Exception exception){
                throw new IllegalArgumentException("Could not register listener "+listener,exception);
            }
        }
    }

    @Override
    public void unregister(Object listener) {
        eventBus.unsubscribe(listener);
    }

    public <E> void registerMangedEvent(Class<E> eventClass, Consumer<E> manager){
        this.mangedEvents.put(eventClass,manager);
    }

    @SuppressWarnings("unchecked")
    @Internal
    private void inject(){
        synchronized (this){
            this.listenersByPlugin = ReflectionUtil.getFieldValue(original,"listenersByPlugin",Multimap.class);

            Object bus = ReflectionUtil.getFieldValue(original,"eventBus");
            ReflectionUtil.changeFieldValue(original,"eventBus",this);
            Map<Class<?>, Map<Byte, Map<Object, Method[]>>> listeners = ReflectionUtil.getFieldValue(bus,"byListenerAndPriority",Map.class);

            for (Map.Entry<Class<?>, Map<Byte, Map<Object, Method[]>>> entry : listeners.entrySet()) {
                Class<?> eventClass = entry.getKey();
                for (Map.Entry<Byte, Map<Object, Method[]>> entry2 : entry.getValue().entrySet()) {
                    byte priority = entry2.getKey();
                    for (Map.Entry<Object, Method[]> entry3 : entry2.getValue().entrySet()) {
                        Object listener = entry3.getKey();
                        for (Method method : entry3.getValue()) {
                            Plugin plugin = findOwner(listener);
                            eventBus.addExecutor(eventClass,new MethodEventExecutor(plugin!=null?
                                    new PluginObjectOwner(plugin):ObjectOwner.SYSTEM
                                    ,priority,ExecutionType.BLOCKING,listener,eventClass,method));
                        }
                    }
                }
            }
        }
    }

    @Internal
    private Plugin findOwner(Object listener){
        for (Map.Entry<Plugin, Listener> entry : this.listenersByPlugin.entries()) {
            if(entry.getValue().equals(listener)) return entry.getKey();
        }
        return null;//throw new IllegalArgumentException("McNative mapping error (BungeeCord -> McNative)");
    }

}
