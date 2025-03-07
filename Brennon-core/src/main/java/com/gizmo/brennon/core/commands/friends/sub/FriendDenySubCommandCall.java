package com.gizmo.brennon.core.commands.friends.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.storage.dao.FriendsDao;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class FriendDenySubCommandCall implements CommandCall
{

    public static void removeFriendRequest( final UserStorage storage, final User user, final User target )
    {
        BuX.getApi().getStorageManager().getDao().getFriendsDao().removeFriendRequest( user.getUuid(), storage.getUuid() );
        user.sendLangMessage(
            "friends.deny.denied",
            MessagePlaceholders.create().append( "user", storage.getUserName() )
        );

        if ( target != null )
        {
            target.sendLangMessage( "friends.deny.request-denied", MessagePlaceholders.create().append( "name", user.getName() ) );
        }
        else if ( BuX.getApi().getPlayerUtils().isOnline( storage.getUserName() ) )
        {
            BuX.getInstance().getJobManager().executeJob( new UserLanguageMessageJob(
                storage.getUserName(),
                "friends.deny.request-denied",
                MessagePlaceholders.create().append( "name", user.getName() )
            ) );
        }
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() < 1 )
        {
            user.sendLangMessage( "friends.deny.usage" );
            return;
        }
        final String name = args.get( 0 );
        final FriendsDao dao = BuX.getApi().getStorageManager().getDao().getFriendsDao();
        final Optional<User> optionalTarget = BuX.getApi().getUser( name );
        final UserStorage storage = Utils.getUserStorageIfUserExists( optionalTarget.orElse( null ), name );

        if ( storage == null )
        {
            user.sendLangMessage( "never-joined" );
            return;
        }

        if ( !dao.hasIncomingFriendRequest( user.getUuid(), storage.getUuid() ).join() )
        {
            user.sendLangMessage( "friends.deny.no-request", MessagePlaceholders.create().append( "user", name ) );
            return;
        }

        removeFriendRequest( storage, user, optionalTarget.orElse( null ) );
    }

    @Override
    public String getDescription()
    {
        return "Denies an incoming friend request.";
    }

    @Override
    public String getUsage()
    {
        return "/friend deny (user)";
    }
}
