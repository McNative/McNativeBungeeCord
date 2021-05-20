/*
 * (C) Copyright 2021 The PretronicLibraries Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 12.05.21, 19:34
 * @web %web%
 *
 * The PretronicLibraries Project is under the Apache License, version 2.0 (the "License");
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

package org.mcnative.runtime.bungeecord.plugin.dependency;

import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import net.pretronic.libraries.utility.reflect.ReflectException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class LegacyReflectedDependencyClassLoader implements DependencyClassLoader {

    private final static Method METHOD_ADD_URL;

    static {
        try {
            METHOD_ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            METHOD_ADD_URL.setAccessible(true);
        } catch (NoSuchMethodException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Override
    public ClassLoader load(ClassLoader parent, URL location) {
        try {
            if(parent == null) parent = getClass().getClassLoader();
            METHOD_ADD_URL.invoke(parent, location);
            return parent;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new ReflectException(exception);
        }
    }
}
