package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.ChatSyncJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

public class ChatSyncJobHandler extends AbstractJobHandler
{

    @JobHandler
    void handleChatSyncJob( final ChatSyncJob job )
    {
        ConfigFiles.SERVERGROUPS.getServer( job.getServerGroupName() ).ifPresent( serverGroup ->
        {
            for ( IProxyServer server : serverGroup.getServers() )
            {
                if ( job.getServerToSkip() != null && job.getServerToSkip().equals( server.getName() ) )
                {
                    continue;
                }

                for ( User user : server.getUsers() )
                {
                    user.sendRawColorMessage( job.getMessage() );
                }
            }
        } );
    }
}
