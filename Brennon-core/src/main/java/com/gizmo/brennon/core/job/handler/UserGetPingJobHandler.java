package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.UserGetPingJob;
import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

public class UserGetPingJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeGetUserPingJob( final UserGetPingJob job )
    {
        job.getTargetUser().ifPresent( target -> BuX.getInstance().getJobManager().executeJob(
            new UserLanguageMessageJob(
                job,
                "general-commands.ping.other",
                MessagePlaceholders.create()
                    .append( "target", target.getName() )
                    .append( "targetPing", target.getPing() )
            )
        ) );
    }
}
