package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.BUEvent;
import com.gizmo.brennon.core.api.job.jobs.ExecuteNetworkStaffEventJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class ExecuteNetworkStaffEventJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void executeNetworkStaffEventJobHandler( final ExecuteNetworkStaffEventJob job )
    {
        final Class<?> clazz = Class.forName( job.getClassName() );
        final BUEvent event = (BUEvent) clazz.getConstructors()[0].newInstance( job.getUserName(), job.getUuid(), job.getStaffRank() );

        BuX.getApi().getEventLoader().launchEvent( event );
    }
}
