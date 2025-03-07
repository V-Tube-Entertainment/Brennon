package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyAddJoinRequestJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyAddJoinRequestJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyAddJoinRequestJob( final PartyAddJoinRequestJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            party.getJoinRequests().add( job.getJoinRequest() );
        } );
    }
}
