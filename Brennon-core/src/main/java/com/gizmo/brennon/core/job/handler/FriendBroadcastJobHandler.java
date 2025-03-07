package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.FriendBroadcastJob;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

public class FriendBroadcastJobHandler
{

    @JobHandler
    void handleFriendBroadcastJob( final FriendBroadcastJob job )
    {
        job.getReceivers().forEach( user -> user.sendLangMessage(
            "friends.broadcast.message",
            MessagePlaceholders.create()
                .append( "user", job.getSenderName() )
                .append( "message", job.getMessage() )
        ) );
    }
}
