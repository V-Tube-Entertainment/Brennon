package com.gizmo.brennon.core.api.placeholder.event;

import com.gizmo.brennon.core.api.placeholder.placeholders.PlaceHolder;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode( callSuper = true )
public class InputPlaceHolderEvent extends PlaceHolderEvent
{

    private final String argument;

    public InputPlaceHolderEvent( User user, PlaceHolder placeHolder, String message, String argument )
    {
        super( user, placeHolder, message );

        this.argument = argument;
    }
}