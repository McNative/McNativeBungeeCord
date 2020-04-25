/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.04.20, 12:22
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

package org.mcnative.common.player.tablist;

import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.player.PlayerDesign;

public class SimpleTablistEntry implements TablistEntry{

    private final String name;
    private final PlayerDesign design;

    public SimpleTablistEntry(String name, PlayerDesign design) {
        this.name = name;
        this.design = design;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PlayerDesign getDesign() {
        return design;
    }

    @Override
    public PlayerDesign getDesign(MinecraftPlayer player) {
        return design;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }
}