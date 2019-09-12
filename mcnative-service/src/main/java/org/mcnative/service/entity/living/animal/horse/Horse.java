/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 27.08.19, 18:11
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

package org.mcnative.service.entity.living.animal.horse;

import org.mcnative.service.inventory.type.ArmorableHorseInventory;

public interface Horse extends AbstractHorse {

    Color getColor();

    void setColor(Color color);

    Style getStyle();

    void setStyle(Style style);

    @Override
    ArmorableHorseInventory getInventory();

    enum Color {

        WHITE,
        CREAMY,
        CHESTNUT,
        BROWN,
        BLACK,
        GRAY,
        DARK_BROWN;
    }

    enum Style {

        NONE,
        WHITE,
        WHITEFIELD,
        WHITE_DOTS,
        BLACK_DOTS,;
    }
}