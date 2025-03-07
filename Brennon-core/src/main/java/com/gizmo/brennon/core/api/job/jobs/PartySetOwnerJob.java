package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.party.Party;
import lombok.Data;

import java.util.UUID;

@Data
public class PartySetOwnerJob implements MultiProxyJob
{

    private final Party party;
    private final UUID uuid;
    private final boolean owner;

    @Override
    public boolean isAsync()
    {
        return true;
    }

}
