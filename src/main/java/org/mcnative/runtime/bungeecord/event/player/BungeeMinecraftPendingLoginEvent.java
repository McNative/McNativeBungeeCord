/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.05.20, 09:31
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

package org.mcnative.runtime.bungeecord.event.player;

import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.mcnative.runtime.api.connection.PendingConnection;
import org.mcnative.runtime.api.event.player.login.MinecraftPlayerPendingLoginEvent;
import org.mcnative.runtime.api.text.components.MessageComponent;

public class BungeeMinecraftPendingLoginEvent implements MinecraftPlayerPendingLoginEvent {

    private final PendingConnection connection;

    private MessageComponent<?> cancelReason;
    private VariableSet variables;
    private boolean canceled;

    public BungeeMinecraftPendingLoginEvent(PendingConnection connection) {
        this.connection = connection;
    }

    @Override
    public PendingConnection getConnection() {
        return connection;
    }

    @Override
    public MessageComponent<?> getCancelReason() {
        return cancelReason;
    }

    @Override
    public VariableSet getCancelReasonVariables() {
        return variables;
    }

    @Override
    public void setCancelReason(MessageComponent<?> cancelReason, VariableSet variables) {
        this.cancelReason = cancelReason;
        this.variables = variables;
        setCancelled(true);
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.canceled = canceled;
    }
}
