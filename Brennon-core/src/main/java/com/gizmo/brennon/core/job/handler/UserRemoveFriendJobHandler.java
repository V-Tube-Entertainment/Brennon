package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.UserRemoveFriendJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

public class UserRemoveFriendJobHandler extends AbstractJobHandler
{

    @JobHandler
    void handleRemoveFriendJob( final UserRemoveFriendJob job )
    {
        job.getUser().ifPresent( user ->
        {
            user.getFriends().removeIf( data -> data.getFriend().equalsIgnoreCase( job.getFriendName() ) );
            user.sendLangMessage( "friends.remove.friend-removed", MessagePlaceholders.create().append( "user", job.getFriendName() ) );
        } );
    }
}
