package com.gizmo.brennon.core.api.placeholder.impl;

import com.gizmo.brennon.core.api.placeholder.PlaceHolderAPI;
import com.gizmo.brennon.core.api.placeholder.event.handler.InputPlaceHolderEventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class InputPlaceHolderImpl extends InputPlaceHolderEventHandler
{

    private final boolean requiresUser;
    private final String prefix;

    public void register()
    {
        PlaceHolderAPI.addPlaceHolder( requiresUser, prefix, this );
    }
}
