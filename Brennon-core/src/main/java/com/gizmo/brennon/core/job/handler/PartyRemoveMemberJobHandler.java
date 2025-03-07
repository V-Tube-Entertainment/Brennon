package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyRemoveMemberJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyRemoveMemberJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyRemoveMemberJob( final PartyRemoveMemberJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            party.getPartyMembers().removeIf( member -> job.getPartyMember().getUuid().equals( member.getUuid() ) );
        } );
    }
}
