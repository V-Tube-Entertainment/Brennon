package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.party.Party;
import lombok.Data;

@Data
public class PartyCreationJob implements MultiProxyJob
{

    private final Party party;

    @Override
    public boolean isAsync()
    {
        return true;
    }

}
