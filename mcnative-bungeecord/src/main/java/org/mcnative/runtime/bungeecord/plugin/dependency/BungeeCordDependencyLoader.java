package org.mcnative.runtime.bungeecord.plugin.dependency;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicInteger;

public class BungeeCordDependencyLoader implements DependencyClassLoader {

    private final AtomicInteger id;

    public BungeeCordDependencyLoader() {
        this.id = new AtomicInteger();
    }

    @Override
    public ClassLoader load(ClassLoader classLoader, URL url) {
        PluginDescription description = new PluginDescription();
        description.setName("McNative Dependency ("+id.incrementAndGet()+")");
        description.setVersion("");
        description.setMain("reflected");

        try{
            Class<?> loaderClass = Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
            Constructor<?> constructor = loaderClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            URLClassLoader loader;

            //ClassLoader
            if(constructor.getParameterCount() == 4){
                loader = (URLClassLoader) constructor.newInstance(ProxyServer.getInstance(), description, new File(url.toURI()),ProxyServer.class.getClassLoader());
            }else {
                loader = (URLClassLoader) constructor.newInstance(ProxyServer.getInstance(), description, url);
                //Change parent class loader to root loader
                ReflectionUtil.changeFieldValue(ClassLoader.class,loader,"parent",ProxyServer.class.getClassLoader());
            }
            return loader;
        }catch (Exception e){
            throw new UnsupportedOperationException(e);
        }
    }
}
