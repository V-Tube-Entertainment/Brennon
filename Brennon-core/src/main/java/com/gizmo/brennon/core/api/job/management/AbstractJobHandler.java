package com.gizmo.brennon.core.api.job.management;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.Job;

public class AbstractJobHandler
{

    protected void executeJob( final Job job )
    {
        BuX.getInstance().getJobManager().executeJob( job );
    }
}
