package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartySetPartyMemberRoleJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import lombok.SneakyThrows;

public class PartySetPartyMemberRoleHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartySetPartyMemberRoleJob( final PartySetPartyMemberRoleJob job )
    {
        BuX.getInstance().getPartyManager().getCurrentPartyByUuid( job.getParty().getUuid() ).ifPresent( party ->
        {
            for ( PartyMember partyMember : party.getPartyMembers() )
            {
                if ( partyMember.getUuid().equals( job.getUuid() ) )
                {
                    partyMember.setPartyRole( ConfigFiles.PARTY_CONFIG.findPartyRole( job.getPartyRole() )
                        .orElse( ConfigFiles.PARTY_CONFIG.getDefaultRole() ) );
                }
            }
        } );
    }
}
