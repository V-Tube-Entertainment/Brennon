package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * This event will be executed when a plugin message is received.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode( callSuper = true )
public class UserPluginMessageReceiveEvent extends AbstractEvent
{

    private final User user;
    private final String channel;
    private final byte[] message;

}