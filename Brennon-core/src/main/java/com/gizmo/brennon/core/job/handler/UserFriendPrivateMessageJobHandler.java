package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.event.events.user.UserFriendPrivateMessageEvent;
import com.gizmo.brennon.core.api.friends.FriendSetting;
import com.gizmo.brennon.core.api.job.jobs.ExecuteEventJob;
import com.gizmo.brennon.core.api.job.jobs.UserFriendPrivateMessageJob;
import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.user.UserStorageKey;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

public class UserFriendPrivateMessageJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeUserFriendPrivateMessageJob( final UserFriendPrivateMessageJob job )
    {
        job.getTargetUser().ifPresent( user ->
        {
            if ( user.isMsgToggled() )
            {
                executeJob( new UserLanguageMessageJob( job, "general-commands.msgtoggle.not-receiving-pms" ) );
                return;
            }

            if ( !user.getFriendSettings().getSetting( FriendSetting.MESSAGES ) )
            {
                executeJob( new UserLanguageMessageJob( job, "friends." + job.getType().toString().toLowerCase() + ".disallowed" ) );
                return;
            }

            if ( user.isVanished() )
            {
                executeJob( new UserLanguageMessageJob( job, "offline" ) );
                return;
            }

            if ( user.getStorage().getIgnoredUsers().stream().anyMatch( ignored -> ignored.equalsIgnoreCase( job.getUserName() ) ) )
            {
                executeJob( new UserLanguageMessageJob( job, "friends." + job.getType().toString().toLowerCase() + ".ignored" ) );
                return;
            }

            user.getStorage().setData( UserStorageKey.FRIEND_MSG_LAST_USER, job.getUserName() );

            user.sendLangMessage(
                false,
                "friends." + job.getType().toString().toLowerCase() + ".format.receive",
                MessagePlaceholders.create()
                    .append( "sender", job.getUserName() )
                    .append( "message", job.getMessage() )
            );

            executeJob( new UserLanguageMessageJob(
                job,
                false,
                "friends." + job.getType().toString().toLowerCase() + ".format.send",
                MessagePlaceholders.create()
                    .append( "receiver", user.getName() )
                    .append( "message", job.getMessage() )
            ) );

            executeJob( new ExecuteEventJob(
                UserFriendPrivateMessageEvent.class,
                job.getUserName(),
                job.getTargetName(),
                job.getMessage()
            ) );
        } );
    }
}
