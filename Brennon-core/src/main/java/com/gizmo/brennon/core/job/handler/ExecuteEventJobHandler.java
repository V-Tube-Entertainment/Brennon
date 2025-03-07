package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.BUEvent;
import com.gizmo.brennon.core.api.job.jobs.ExecuteEventJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class ExecuteEventJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void executeEventJobHandler( final ExecuteEventJob job )
    {
        final Class<?> clazz = Class.forName( job.getClassName() );
        final BUEvent event = (BUEvent) clazz.getConstructors()[0].newInstance( job.getParameters() );

        BuX.getApi().getEventLoader().launchEvent( event );
    }
}
