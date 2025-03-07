package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import lombok.Data;

@Data
public class PartyAddMemberJob implements MultiProxyJob
{

    private final Party party;
    private final PartyMember partyMember;

    @Override
    public boolean isAsync()
    {
        return true;
    }

}
