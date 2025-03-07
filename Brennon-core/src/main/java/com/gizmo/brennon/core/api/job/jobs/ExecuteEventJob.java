package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.event.event.BUEvent;
import com.gizmo.brennon.core.api.job.MultiProxyJob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ExecuteEventJob implements MultiProxyJob
{

    private final String className;
    private final Object[] parameters;

    public ExecuteEventJob( final Class<? extends BUEvent> clazz, final Object... parameters )
    {
        this.className = clazz.getName();
        this.parameters = parameters;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
