package com.gizmo.brennon.core.commands.general;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;

public class ShoutCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() == 0 )
        {
            user.sendLangMessage( "general-commands.shout.usage" );
            return;
        }

        final String message = String.join( " ", args );

        if ( message.isBlank() )
        {

            return;
        }

        String shoutMessagePath = "general-commands.shout.shout-broadcast";
        if ( user.hasPermission( ConfigFiles.GENERALCOMMANDS.getConfig().getString( "shout.staff-permission" ) ) )
        {
            shoutMessagePath += "-staff";
        }

        BuX.getApi().langBroadcast(
            shoutMessagePath,
            MessagePlaceholders.create()
                .append( "user", user.getName() )
                .append( "servername", user.getServerName() )
                .append( "message", message )
        );
    }

    @Override
    public String getDescription()
    {
        return "Sends a global shout. This is a simplified version of /announce that can be used as a donator perk. Staff can have a custom shout format.";
    }

    @Override
    public String getUsage()
    {
        return "/shout (message)";
    }
}
