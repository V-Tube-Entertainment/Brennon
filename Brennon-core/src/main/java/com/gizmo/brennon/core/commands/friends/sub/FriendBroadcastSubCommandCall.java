package com.gizmo.brennon.core.commands.friends.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.friends.FriendSetting;
import com.gizmo.brennon.core.api.job.jobs.FriendBroadcastJob;
import com.gizmo.brennon.core.api.user.interfaces.User;

import java.util.List;
import java.util.stream.Collectors;

public class FriendBroadcastSubCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() < 1 )
        {
            user.sendLangMessage( "friends.broadcast.usage" );
            return;
        }
        if ( !user.getFriendSettings().getSetting( FriendSetting.FRIEND_BROADCAST ) )
        {
            user.sendLangMessage( "friends.broadcast.disabled" );
            return;
        }

        final String message = String.join( " ", args );
        BuX.getInstance().getJobManager().executeJob( new FriendBroadcastJob(
            user.getUuid(),
            user.getName(),
            message,
            user.getFriends()
                .stream()
                .map( FriendData::getFriend )
                .collect( Collectors.toList() )
        ) );
    }

    @Override
    public String getDescription()
    {
        return "Broadcasts a message to all your online friends.";
    }

    @Override
    public String getUsage()
    {
        return "/friend broadcast (message)";
    }
}
