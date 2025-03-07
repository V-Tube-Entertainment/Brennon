package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * This event is being executed when a User has successfully been loaded in.
 */
@AllArgsConstructor
@EqualsAndHashCode( callSuper = true )
public class UserLoadEvent extends AbstractEvent
{

    @Getter
    private User user;

}