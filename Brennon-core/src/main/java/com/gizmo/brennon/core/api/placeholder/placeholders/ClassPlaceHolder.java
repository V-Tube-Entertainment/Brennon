package com.gizmo.brennon.core.api.placeholder.placeholders;

import com.gizmo.brennon.core.api.placeholder.event.handler.PlaceHolderEventHandler;

public abstract class ClassPlaceHolder extends PlaceHolder implements PlaceHolderEventHandler
{

    public ClassPlaceHolder( String placeHolder, boolean requiresUser )
    {
        super( placeHolder, requiresUser, null );
        super.setEventHandler( this );
    }
}