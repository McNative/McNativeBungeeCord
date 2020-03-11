/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Philipp Elvin Friedhoff
 * @since 09.03.20, 20:23
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

package org.mcnative.common.event.service;

import net.prematic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.event.MinecraftEvent;

public class ServiceEvent implements MinecraftEvent {

    private final Object service;
    private final ObjectOwner owner;

    protected ServiceEvent(Object service, ObjectOwner owner) {
        this.service = service;
        this.owner = owner;
    }

    public Object getService() {
        return service;
    }

    public <T> T getService(Class<T> serviceClass) {
        if(service.getClass() == serviceClass) {
            return (T) getService();
        }
        throw new IllegalArgumentException("Service is not an instance of " + serviceClass.getName());
    }

    public Class<?> getServiceClass() {
        return getService().getClass();
    }

    public boolean isService(Class<?> serviceClass) {
        return getService().getClass() == serviceClass;
    }

    public ObjectOwner getOwner() {
        return owner;
    }
}