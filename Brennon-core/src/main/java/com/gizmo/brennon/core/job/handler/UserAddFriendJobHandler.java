package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.UserAddFriendJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

public class UserAddFriendJobHandler extends AbstractJobHandler
{

    @JobHandler
    void handleAddFriendJob( final UserAddFriendJob job )
    {
        job.getUser().ifPresent( user ->
        {
            user.sendLangMessage( "friends.accept.request-accepted", MessagePlaceholders.create().append( "user", job.getFriendName() ) );
            user.getFriends().add( job.getAsFriendData() );
        } );
    }
}
