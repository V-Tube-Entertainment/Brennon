package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.AnnounceJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.commands.general.AnnounceCommandCall;

public class AnnounceJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeAnnounceJob( final AnnounceJob job )
    {
        AnnounceCommandCall.sendAnnounce( job.getTypes(), job.getMessage() );
    }
}
