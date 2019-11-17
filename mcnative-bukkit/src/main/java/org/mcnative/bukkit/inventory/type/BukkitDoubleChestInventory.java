/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 14.11.19, 17:10
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

package org.mcnative.bukkit.inventory.type;

import org.mcnative.service.inventory.InventoryOwner;
import org.mcnative.service.inventory.type.ChestInventory;
import org.mcnative.service.inventory.type.DoubleChestInventory;

public class BukkitDoubleChestInventory extends BukkitChestInventory<org.bukkit.inventory.DoubleChestInventory> implements DoubleChestInventory {

    public BukkitDoubleChestInventory(InventoryOwner owner, org.bukkit.inventory.DoubleChestInventory original) {
        super(owner, original);
    }

    @Override
    public ChestInventory getLeftSide() {
        return new BukkitChestInventory<>(getOwner(), this.original.getLeftSide());
    }

    @Override
    public ChestInventory getRightSide() {
        return new BukkitChestInventory<>(getOwner(), this.original.getRightSide());
    }
}
