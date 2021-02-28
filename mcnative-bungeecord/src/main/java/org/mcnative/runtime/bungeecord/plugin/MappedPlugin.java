/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 26.11.19, 19:46
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

import net.md_5.bungee.api.plugin.Plugin;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.EmptyDocument;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.description.PluginVersion;
import net.pretronic.libraries.plugin.description.dependency.Dependency;
import net.pretronic.libraries.plugin.description.mainclass.MainClass;
import net.pretronic.libraries.plugin.description.mainclass.SingleMainClass;
import org.mcnative.runtime.bungeecord.BungeeCordMcNativeBootstrap;
import org.mcnative.runtime.bungeecord.McNativeLauncher;
import org.mcnative.runtime.api.McNative;
import org.mcnative.runtime.api.plugin.MinecraftPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class MappedPlugin extends MinecraftPlugin {

    private final Plugin plugin;

    public MappedPlugin(Plugin plugin) {
        this.plugin = plugin;
        String messageModule = isMcNative() ? "McNative" : null;
        initialize(new Description(plugin.getDescription(),messageModule),null
                ,new JdkPretronicLogger(plugin.getLogger()), McNative.getInstance());
    }

    private boolean isMcNative(){
        return plugin instanceof BungeeCordMcNativeBootstrap || plugin instanceof McNativeLauncher.DummyPlugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public McNative getRuntime() {
        return McNative.getInstance();
    }

    @Override
    public int hashCode() {
        return plugin.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else if(obj instanceof MappedPlugin) return plugin.equals(((MappedPlugin) obj).getPlugin());
        else if(obj instanceof Plugin) return plugin.equals(obj);
        else return plugin.equals(obj);
    }

    @Override
    public String toString() {
        return plugin.toString();
    }

    public static class Description implements PluginDescription {

        private final net.md_5.bungee.api.plugin.PluginDescription original;
        private final String messageModule;

        public Description(net.md_5.bungee.api.plugin.PluginDescription original,String messageModule) {
            this.original = original;
            this.messageModule = messageModule;
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public String getCategory() {
            return "General";
        }

        @Override
        public String getDescription() {
            return original.getDescription();
        }

        @Override
        public String getAuthor() {
            return original.getAuthor();
        }

        @Override
        public String getWebsite() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public PluginVersion getVersion() {
            return new PluginVersion(original.getVersion(),1,0,0,0,"");
        }

        @Override
        public PluginVersion getLatestVersion() {
            return null;
        }

        @Override
        public void setLatestVersion(PluginVersion pluginVersion) {
            //Unused
        }

        @Override
        public String getMessageModule() {
            return messageModule;
        }

        @Override
        public MainClass getMain() {
            return new SingleMainClass(original.getMain());
        }

        @Override
        public Collection<Dependency> getDependencies() {
            return Collections.emptyList();//@Todo implement
        }

        @Override
        public Collection<String> getProviders() {
            return Collections.emptyList();
        }

        @Override
        public Document getProperties() {
            return EmptyDocument.newDocument();
        }
    }
}
