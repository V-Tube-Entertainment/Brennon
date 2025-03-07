package com.gizmo.brennon.core.commands.general;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;

public class StaffAnnouncementCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() == 0 )
        {
            user.sendLangMessage( "general-commands.staffannouncement.usage" );
            return;
        }
        final String message = String.join( " ", args );

        BuX.getApi().langPermissionBroadcast(
            "general-commands.staffannouncement.broadcast",
            ConfigFiles.GENERALCOMMANDS.getConfig().getString( "staffannouncement.receive-permission" ),
            MessagePlaceholders.create()
                .append( "broadcaster", user.getName() )
                .append( "message", message )
        );
    }

    @Override
    public String getDescription()
    {
        return "Sends an announcement only to people that have a specific permission.";
    }

    @Override
    public String getUsage()
    {
        return "/staffannouncement (message)";
    }
}
