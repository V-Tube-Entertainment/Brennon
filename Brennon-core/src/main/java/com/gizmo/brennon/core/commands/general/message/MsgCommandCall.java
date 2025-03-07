package com.gizmo.brennon.core.commands.general.message;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.PrivateMessageType;
import com.gizmo.brennon.core.api.job.jobs.UserPrivateMessageJob;
import com.gizmo.brennon.core.api.user.UserStorageKey;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.StaffUtils;

import java.util.List;

public class MsgCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( user.isMsgToggled() )
        {
            user.sendLangMessage( "general-commands.msgtoggle.pm-cmd-disabled" );
            return;
        }
        if ( args.size() < 2 )
        {
            user.sendLangMessage( "general-commands.msg.usage" );
            return;
        }
        final String name = args.get( 0 );

        if ( user.getName().equalsIgnoreCase( name ) )
        {
            user.sendLangMessage( "general-commands.msg.self-msg" );
            return;
        }

        if ( BuX.getApi().getPlayerUtils().isOnline( name ) && !StaffUtils.isHidden( name ) )
        {
            final String message = String.join( " ", args.subList( 1, args.size() ) );

            user.getStorage().setData( UserStorageKey.MSG_LAST_USER, name );

            BuX.getInstance().getJobManager().executeJob( new UserPrivateMessageJob(
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
        return "Sends a private message to a user.";
    }

    @Override
    public String getUsage()
    {
        return "/msg (user) (message)";
    }
}
