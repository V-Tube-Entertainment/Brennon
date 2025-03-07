package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * This event gets called if an User was successfully saved and logged out.
 */
@AllArgsConstructor
@EqualsAndHashCode( callSuper = true )
public class UserUnloadEvent extends AbstractEvent
{

    @Getter
    private final User user;

}