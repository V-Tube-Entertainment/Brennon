package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyJoinRequest;
import lombok.Data;

@Data
public class PartyAddJoinRequestJob implements MultiProxyJob
{

    private final Party party;
    private final PartyJoinRequest joinRequest;

    @Override
    public boolean isAsync()
    {
        return true;
    }

}
