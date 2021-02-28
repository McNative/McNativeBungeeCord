package org.mcnative.runtime.bungeecord.shared;

import java.util.function.Consumer;

public interface McNativeBridgedEventBus {

    <E> void registerMangedEvent(Class<E> eventClass, Consumer<E> manager);

}
