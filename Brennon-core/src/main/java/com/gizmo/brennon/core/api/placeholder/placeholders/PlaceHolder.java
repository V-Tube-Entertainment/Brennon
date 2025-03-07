package com.gizmo.brennon.core.api.placeholder.placeholders;

import com.gizmo.brennon.core.api.placeholder.event.handler.PlaceHolderEventHandler;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@ToString
@EqualsAndHashCode
public abstract class PlaceHolder
{

    protected String placeHolderName;
    @Accessors( fluent = true )
    protected boolean requiresUser;
    @Setter
    protected PlaceHolderEventHandler eventHandler;

    public PlaceHolder( String placeHolderName, boolean requiresUser, PlaceHolderEventHandler eventHandler )
    {
        this.placeHolderName = placeHolderName;
        this.requiresUser = requiresUser;
        this.eventHandler = eventHandler;
    }

    public boolean requiresUser()
    {
        return requiresUser;
    }

    public abstract String format( User user, String message );
}
