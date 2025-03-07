package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.ChatLockJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.commands.general.ChatLockCommandCall;

public class ChatLockJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeChatLockJob( final ChatLockJob job )
    {
        ChatLockCommandCall.lockChat( job.getServerName(), job.getBy() );
    }
}
