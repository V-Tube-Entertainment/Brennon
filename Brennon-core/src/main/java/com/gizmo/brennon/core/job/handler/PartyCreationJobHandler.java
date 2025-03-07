package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyCreationJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyCreationJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyCreationJob( final PartyCreationJob job )
    {
        BuX.getInstance().getPartyManager().registerPartyLocally( job.getParty() );
    }
}
