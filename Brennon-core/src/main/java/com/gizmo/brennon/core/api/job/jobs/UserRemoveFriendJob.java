package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.HasUserJob;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserRemoveFriendJob extends HasUserJob
{

    private final String friendName;

    public UserRemoveFriendJob( final UUID uuid,
                                final String userName,
                                final String friendName )
    {
        super( uuid, userName );

        this.friendName = friendName;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
