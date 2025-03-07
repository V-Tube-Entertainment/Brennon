package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartySetChatModeJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.party.PartyMember;
import lombok.SneakyThrows;

public class PartySetChatModeHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartySetChatModeJob( final PartySetChatModeJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            for ( PartyMember partyMember : party.getPartyMembers() )
            {
                if ( partyMember.getUuid().equals( job.getUuid() ) )
                {
                    partyMember.setChat( job.isChat() );
                }
            }
        } );
    }
}
