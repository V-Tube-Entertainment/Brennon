package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.UserVanishUpdateJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;

public class UserVanishUpdateJobHandler extends AbstractJobHandler
{

    @JobHandler
    void handleUserVanishUpdate( final UserVanishUpdateJob job )
    {
        // update staff vanished value
        BuX.getApi().getStaffMembers()
            .stream()
            .filter( staffUser -> staffUser.getName().equalsIgnoreCase( job.getUserName() ) )
            .forEach( staffUser -> staffUser.setVanished( job.isVanished() ) );

        // update user vanished value
        job.getUser().ifPresent( user -> user.setVanished( job.isVanished() ) );
    }
}
