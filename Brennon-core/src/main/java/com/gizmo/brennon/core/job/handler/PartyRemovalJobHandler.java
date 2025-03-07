package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.PartyRemovalJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import lombok.SneakyThrows;

public class PartyRemovalJobHandler extends AbstractJobHandler
{

    @JobHandler
    @SneakyThrows
    void handlePartyRemovalJob( final PartyRemovalJob job )
    {
        BuX.getInstance().getPartyManager().unregisterPartyLocally( job.getParty() );
    }
}
