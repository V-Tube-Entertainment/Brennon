package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyInvite;
import lombok.Data;

@Data
public class PartyAddInvitationJob implements MultiProxyJob
{

    private final Party party;
    private final PartyInvite partyInvite;

    @Override
    public boolean isAsync()
    {
        return true;
    }

}
