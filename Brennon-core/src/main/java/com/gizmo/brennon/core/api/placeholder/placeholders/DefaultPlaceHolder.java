package com.gizmo.brennon.core.api.placeholder.placeholders;

import com.gizmo.brennon.core.api.placeholder.event.PlaceHolderEvent;
import com.gizmo.brennon.core.api.placeholder.event.handler.PlaceHolderEventHandler;
import com.gizmo.brennon.core.api.user.interfaces.User;

public class DefaultPlaceHolder extends PlaceHolder
{

    public DefaultPlaceHolder( String placeHolder, boolean requiresUser, PlaceHolderEventHandler handler )
    {
        super( placeHolder, requiresUser, handler );
    }

    @Override
    public String format( User user, String message )
    {
        if ( placeHolderName == null || !message.contains( placeHolderName ) )
        {
            return message;
        }
        PlaceHolderEvent event = new PlaceHolderEvent( user, this, message );
        return message.replace( placeHolderName, eventHandler.getReplacement( event ) );
    }
}