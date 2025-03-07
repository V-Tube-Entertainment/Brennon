package com.gizmo.brennon.core.api.event;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.IBuXApi;
import com.gizmo.brennon.core.api.event.event.BUEvent;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class AbstractEvent implements BUEvent
{

    @Override
    public IBuXApi getApi()
    {
        return BuX.getApi();
    }
}