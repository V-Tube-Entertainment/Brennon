package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyRemoveJoinRequestJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyRemoveJoinRequestJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyRemoveJoinRequestJob( final PartyRemoveJoinRequestJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            party.getJoinRequests().removeIf( joinRequest -> job.getJoinRequest().getRequester().equals( joinRequest.getRequester() ) );
        } );
    }
}
