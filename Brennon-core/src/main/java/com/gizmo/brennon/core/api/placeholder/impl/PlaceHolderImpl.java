package com.gizmo.brennon.core.api.placeholder.impl;

import com.gizmo.brennon.core.api.placeholder.PlaceHolderAPI;
import com.gizmo.brennon.core.api.placeholder.event.handler.PlaceHolderEventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class PlaceHolderImpl implements PlaceHolderEventHandler
{

    private final String placeHolder;
    private final boolean requiresUser;

    public void register()
    {
        PlaceHolderAPI.addPlaceHolder( placeHolder, requiresUser, this );
    }
}
