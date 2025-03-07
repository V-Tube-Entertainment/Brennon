package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.OpenGuiJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;

public class OpenGuiJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeOpenGuiJob( final OpenGuiJob job )
    {
        if ( BuX.getInstance().isProtocolizeEnabled() )
        {
            job.getUserByName().ifPresent( user ->
                BuX.getInstance().getProtocolizeManager().getGuiManager().openGui( user, job.getGui(), job.getArgs() )
            );
        }
    }
}
