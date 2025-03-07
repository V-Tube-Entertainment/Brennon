package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
public class PartyWarpMembersJob implements MultiProxyJob
{

    private final UUID partyUuid;
    private final List<UUID> membersToWarp;
    private final String targetServer;

    @Override
    public boolean isAsync()
    {
        return true;
    }

    public List<User> getOnlineMembersToWarp()
    {
        return membersToWarp
            .stream()
            .map( BuX.getApi()::getUser )
            .filter( Optional::isPresent )
            .map( Optional::get )
            .toList();
    }
}
