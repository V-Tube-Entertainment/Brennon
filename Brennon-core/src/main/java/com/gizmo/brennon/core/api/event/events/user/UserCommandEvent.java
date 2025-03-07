package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.event.event.Cancellable;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

/**
 * This event is being executed upon User Command execute.
 */
@Data
@EqualsAndHashCode( callSuper = true )
public class UserCommandEvent extends AbstractEvent implements Cancellable
{

    private User user;
    private String command;
    private boolean cancelled = false;

    public UserCommandEvent( final User user, final String command )
    {
        this.user = user;
        this.command = command;
    }

    public String getActualCommand()
    {
        return command.split( " " )[0].toLowerCase();
    }

    public String[] getArguments()
    {
        final String[] arguments = command.split( " " );

        return Arrays.copyOfRange( arguments, 1, arguments.length );
    }
}
