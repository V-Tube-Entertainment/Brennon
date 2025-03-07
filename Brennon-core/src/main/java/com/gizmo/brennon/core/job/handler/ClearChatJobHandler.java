package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.ClearChatJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.commands.general.ClearChatCommandCall;

public class ClearChatJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeClearChatJob( final ClearChatJob job )
    {
        ClearChatCommandCall.clearChat( job.getServerName(), job.getBy() );
    }
}
