package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyWarpMembersJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import lombok.SneakyThrows;

public class PartyWarpMembersJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyWarpMembersJob( final PartyWarpMembersJob job )
    {
        final IProxyServer server = BuX.getInstance().serverOperations().getServerInfo( job.getTargetServer() );

        if ( server != null )
        {
            job.getOnlineMembersToWarp().forEach( user ->
            {
                user.sendToServer( server );
                user.sendLangMessage( "party.warp.warped", MessagePlaceholders.create().append( "server", job.getTargetServer() ) );
            } );
        }
    }
}
