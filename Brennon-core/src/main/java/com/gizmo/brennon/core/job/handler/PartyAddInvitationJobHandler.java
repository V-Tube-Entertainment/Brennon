package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyAddInvitationJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyAddInvitationJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyAddInvitationJob( final PartyAddInvitationJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            party.getSentInvites().add( job.getPartyInvite() );
        } );
    }
}
