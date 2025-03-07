package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyRemoveInvitationJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyRemoveInvitationJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyRemoveInvitationJob( final PartyRemoveInvitationJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            party.getSentInvites().removeIf( invite -> job.getPartyInvite().getInvitee().equals( invite.getInvitee() ) );
        } );
    }
}
