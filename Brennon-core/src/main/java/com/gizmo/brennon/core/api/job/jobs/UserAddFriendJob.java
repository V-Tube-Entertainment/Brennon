package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.job.HasUserJob;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
public class UserAddFriendJob extends HasUserJob
{

    private final UUID friendUuid;
    private final String friendName;
    private final Date friendsSince;
    private final Date lastLogout;

    public UserAddFriendJob( final UUID uuid,
                             final String userName,
                             final UUID friendUuid,
                             final String friendName,
                             final Date friendsSince,
                             final Date lastLogout )
    {
        super( uuid, userName );

        this.friendUuid = friendUuid;
        this.friendName = friendName;
        this.friendsSince = friendsSince;
        this.lastLogout = lastLogout;
    }

    public FriendData getAsFriendData()
    {
        return new FriendData( friendUuid, friendName, friendsSince, lastLogout );
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
