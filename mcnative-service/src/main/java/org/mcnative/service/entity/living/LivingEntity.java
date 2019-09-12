/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.08.19, 21:39
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

package org.mcnative.service.entity.living;

import org.mcnative.service.entity.Damageable;
import org.mcnative.service.entity.Entity;
import org.mcnative.service.entity.projectile.ProjectileSource;
import org.mcnative.service.location.Location;
import org.mcnative.service.material.Material;
import org.mcnative.service.world.block.Block;

import java.util.List;
import java.util.Set;

public interface LivingEntity extends Entity, Damageable, ProjectileSource {

    double getEyeHeight();

    double getEyeHeight(boolean ignorePose);

    Location getEyeLocation();

    List<Block> getLineOfSight(Set<Material> transparent, int maxDistance);

    boolean hasLineOfSight(Entity other);

    Block getTargetBlock(Set<Material> transparent, int maxDistance);

    Block getTargetBlock(int maxDistance);

    //Collection<PotionEffect> getActivePotionEffects();

    //PotionEffect getPotionEffect(PotionEffectType type);

    //boolean hasPotionEffect(PotionEffectType type);

    //boolean addPotionEffect(PotionEffect effect);

    //boolean addPotionEffect(PotionEffect effect, boolean force);

    //void removePotionEffect(PotionEffectType type);

    int getHandRaisedTime();

    boolean isHandRaised();
}
