package com.gizmo.brennon.core.commands.friends.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.PrivateMessageType;
import com.gizmo.brennon.core.api.job.jobs.UserFriendPrivateMessageJob;
import com.gizmo.brennon.core.api.user.UserStorageKey;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.StaffUtils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;

public class FriendMsgSubCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() < 2 )
        {
            user.sendLangMessage( "friends.msg.usage" );
            return;
        }
        final String name = args.get( 0 );

        if ( user.getFriends().stream().noneMatch( data -> data.getFriend().equalsIgnoreCase( name ) ) )
        {
            user.sendLangMessage( "friends.msg.not-friend", MessagePlaceholders.create().append( "user", name ) );
            return;
        }

        if ( BuX.getApi().getPlayerUtils().isOnline( name ) && !StaffUtils.isHidden( name ) )
        {
            final String message = String.join( " ", args.subList( 1, args.size() ) );

            user.getStorage().setData( UserStorageKey.FRIEND_MSG_LAST_USER, name );

            BuX.getInstance().getJobManager().executeJob( new UserFriendPrivateMessageJob(
                user.getUuid(),
                user.getName(),
                name,
                message,
                PrivateMessageType.MSG
            ) );
        }
        else
        {
            user.sendLangMessage( "offline" );
        }
    }

    @Override
    public String getDescription()
    {
        return "Allows you to privately message a friend.";
    }

    @Override
    public String getUsage()
    {
        return "/friend msg (user)";
    }
}
