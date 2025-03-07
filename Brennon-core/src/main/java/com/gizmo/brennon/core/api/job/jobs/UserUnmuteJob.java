package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.HasUserJob;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserUnmuteJob extends HasUserJob
{

    private final String serverName;

    public UserUnmuteJob( final UUID uuid, final String userName, final String serverName )
    {
        super( uuid, userName );

        this.serverName = serverName;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
