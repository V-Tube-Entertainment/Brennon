package com.gizmo.brennon.core.commands.friends.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.storage.dao.Dao;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class FriendRemoveRequestSubCommandCall implements CommandCall
{

    public static void removeFriendRequest( final UserStorage storage, final User user, final User target )
    {
        BuX.getApi().getStorageManager().getDao().getFriendsDao().removeFriendRequest( storage.getUuid(), user.getUuid() );
        user.sendLangMessage( "friends.removerequest.removed", storage );

        if ( target != null )
        {
            target.sendLangMessage( "friends.removerequest.request-removed", user );
        }
        else if ( BuX.getApi().getPlayerUtils().isOnline( storage.getUserName() ) )
        {
            BuX.getInstance().getJobManager().executeJob( new UserLanguageMessageJob(
                storage.getUserName(),
                "friends.removerequest.request-removed",
                user.getMessagePlaceholders()
            ) );
        }
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() < 1 )
        {
            user.sendLangMessage( "friends.removerequest.usage" );
            return;
        }
        final String name = args.get( 0 );
        final Dao dao = BuX.getApi().getStorageManager().getDao();
        final Optional<User> optionalTarget = BuX.getApi().getUser( name );
        final UserStorage storage = Utils.getUserStorageIfUserExists( optionalTarget.orElse( null ), name );

        if ( storage == null )
        {
            user.sendLangMessage( "never-joined" );
            return;
        }

        if ( !dao.getFriendsDao().hasOutgoingFriendRequest( user.getUuid(), storage.getUuid() ).join() )
        {
            user.sendLangMessage( "friends.removerequest.no-request", MessagePlaceholders.create().append( "user", name ) );
            return;
        }

        removeFriendRequest( storage, user, optionalTarget.orElse( null ) );
    }

    @Override
    public String getDescription()
    {
        return "Removes an outstanding friend request from / towards a certain user.";
    }

    @Override
    public String getUsage()
    {
        return "/friend removerequest (user)";
    }
}
