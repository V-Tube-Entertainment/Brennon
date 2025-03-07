package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.HasUserJob;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserVanishUpdateJob extends HasUserJob
{

    private final boolean vanished;

    public UserVanishUpdateJob( final UUID uuid,
                                final String userName,
                                final boolean vanished )
    {
        super( uuid, userName );

        this.vanished = vanished;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
