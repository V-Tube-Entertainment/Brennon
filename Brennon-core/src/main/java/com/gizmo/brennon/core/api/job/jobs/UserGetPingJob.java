package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.HasUserJob;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class UserGetPingJob extends HasUserJob
{

    private final String targetName;

    public UserGetPingJob( final UUID uuid,
                           final String userName,
                           final String targetName )
    {
        super( uuid, userName );
        this.targetName = targetName;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }

    public Optional<User> getTargetUser()
    {
        return BuX.getApi().getUser( targetName );
    }
}
