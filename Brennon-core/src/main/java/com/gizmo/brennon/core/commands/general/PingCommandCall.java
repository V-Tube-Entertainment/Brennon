package com.gizmo.brennon.core.commands.general;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.UserGetPingJob;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;

public class PingCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.isEmpty() )
        {
            user.sendLangMessage( "general-commands.ping.message" );
        }
        else
        {
            final String permission = ConfigFiles.GENERALCOMMANDS.getConfig().getString( "ping.permission-other" );
            if ( permission != null
                && !permission.isEmpty()
                && !user.hasPermission( permission ) )
            {
                user.sendLangMessage( "no-permission", MessagePlaceholders.create().append( "permission", permission ) );
                return;
            }

            final String name = args.get( 0 );

            if ( BuX.getApi().getPlayerUtils().isOnline( name ) )
            {
                BuX.getInstance().getJobManager().executeJob(
                    new UserGetPingJob( user.getUuid(), user.getName(), name )
                );
            }
            else
            {
                user.sendLangMessage( "offline" );
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "Shows your (or someone else's) current ping towards the current proxy.";
    }

    @Override
    public String getUsage()
    {
        return "/ping [user]";
    }
}
