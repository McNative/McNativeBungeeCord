/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.08.19, 18:20
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
import net.md_5.bungee.protocol.ProtocolConstants;
import org.mcnative.runtime.api.MinecraftPlatform;
import org.mcnative.runtime.api.protocol.MinecraftEdition;
import org.mcnative.runtime.api.protocol.MinecraftProtocolVersion;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

public class BungeeCordPlatform implements MinecraftPlatform {

    private final File latestLogLocation;
    private final MinecraftProtocolVersion newest;
    private final Collection<MinecraftProtocolVersion> versions;

    public BungeeCordPlatform() {
        this.latestLogLocation = detectLatestLogLocation();
        this.versions = new ArrayList<>();
        this.newest = extractVersions(this.versions);
    }

    @Override
    public String getName() {
        return "BungeeCord";
    }

    @Override
    public String getVersion() {
        return ProxyServer.getInstance().getVersion();
    }

    @Override
    public MinecraftProtocolVersion getProtocolVersion() {
        return newest;
    }

    @Override
    public Collection<MinecraftProtocolVersion> getJoinableProtocolVersions() {
        return versions;
    }

    @Override
    public boolean isProxy() {
        return true;
    }

    @Override
    public boolean isService() {
        return false;
    }

    @Override
    public File getLatestLogLocation() {
        return this.latestLogLocation;
    }

    private File detectLatestLogLocation() {
        File file = new File("proxy.log.0");
        if(!file.exists()) file = new File("logs/latest.log");
        return file;
    }

    private static MinecraftProtocolVersion extractVersions(Collection<MinecraftProtocolVersion> versions){
        MinecraftProtocolVersion newest = MinecraftProtocolVersion.UNKNOWN;
        for (Integer supportedVersionId : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            try{
                MinecraftProtocolVersion version = MinecraftProtocolVersion.of(MinecraftEdition.JAVA,supportedVersionId);
                versions.add(version);
                if(supportedVersionId >= newest.getNumber()) newest = version;
            }catch (Exception exception){
                System.out.println("Protocol version "+supportedVersionId+" not found");
                exception.printStackTrace();
            }
        }
        return newest;
    }
}
