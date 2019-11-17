/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 23.08.19, 22:06
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

package org.mcnative.service.inventory.type;

import org.mcnative.service.inventory.Inventory;
import org.mcnative.service.inventory.item.ItemStack;

public interface PlayerInventory extends Inventory {

    ItemStack getHelmet();

    ItemStack getChestplate();

    ItemStack getLeggings();

    ItemStack getBoots();

    ItemStack[] getArmorContents();

    ItemStack getItemInMainHand();

    ItemStack getItemInOffMainHand();

    int getHeldItemSlot();

    void setHelmet(ItemStack helmet);

    void setChestplate(ItemStack chestplate);

    void setLeggings(ItemStack leggings);

    void setBoots(ItemStack boots);

    void setArmorContents(ItemStack... items);

    void setItemInMainHand(ItemStack stack);

    void setItemInOffHand(ItemStack stack);

    void setHeldItemSlot(int slot);

    void clearArmor();

}
