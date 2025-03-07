package com.gizmo.brennon.core.api.placeholder.event.handler;

import com.gizmo.brennon.core.api.placeholder.event.InputPlaceHolderEvent;
import com.gizmo.brennon.core.api.placeholder.event.PlaceHolderEvent;

public abstract class InputPlaceHolderEventHandler implements PlaceHolderEventHandler
{

    public abstract String getReplacement( InputPlaceHolderEvent event );

    @Override
    public String getReplacement( PlaceHolderEvent event )
    {
        return getReplacement( (InputPlaceHolderEvent) event );
    }
}
