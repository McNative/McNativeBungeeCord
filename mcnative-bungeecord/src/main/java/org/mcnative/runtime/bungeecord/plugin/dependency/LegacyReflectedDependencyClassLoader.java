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

import jdk.internal.loader.URLClassPath;
import net.pretronic.libraries.dependency.loader.DependencyClassLoader;
import net.pretronic.libraries.utility.exception.OperationFailedException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

public class LegacyReflectedDependencyClassLoader implements DependencyClassLoader {

    private final static Field FIELD_UCP;

    static {
        try {
            FIELD_UCP = URLClassLoader.class.getDeclaredField("ucp");
            FIELD_UCP.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Override
    public ClassLoader load(ClassLoader parent, URL location) {
        Unsafe unsafe = ReflectionUtil.getUnsafe();
        if(parent == null) parent = getClass().getClassLoader();
        URLClassPath ucp = (URLClassPath) unsafe.getObject(parent,unsafe.objectFieldOffset(FIELD_UCP));
        if(ucp == null) throw new OperationFailedException("Could not extract ucp from "+parent.getClass());
        ucp.addURL(location);
        return parent;
    }
}
