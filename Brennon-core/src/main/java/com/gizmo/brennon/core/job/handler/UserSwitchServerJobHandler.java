package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.UserSwitchServerJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.commands.general.ServerCommandCall;

public class UserSwitchServerJobHandler extends AbstractJobHandler
{

    @JobHandler
    void handleUserSwitchServerJob( final UserSwitchServerJob job )
    {
        job.getTargetUser().ifPresent( user -> job.getTargetServer().ifPresent( proxyServer ->
            ServerCommandCall.sendToServer( user, proxyServer )
        ) );
    }
}
