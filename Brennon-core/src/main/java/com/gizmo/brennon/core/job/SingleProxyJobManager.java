package com.gizmo.brennon.core.job;

import com.gizmo.brennon.core.api.job.Job;
import com.gizmo.brennon.core.api.job.management.JobManager;

import java.util.concurrent.CompletableFuture;

public class SingleProxyJobManager extends JobManager
{

    @Override
    public CompletableFuture<Void> executeJob( final Job job )
    {
        this.handle( job );
        return CompletableFuture.completedFuture( null );
    }
}
