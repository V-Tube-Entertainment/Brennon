package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.event.Priority;
import com.gizmo.brennon.core.api.event.events.user.UserChatEvent;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.commands.general.StaffChatCommandCall;
import dev.endoy.configuration.api.IConfiguration;

public class StaffChatExecutor implements EventExecutor
{

    @Event( priority = Priority.LOWEST )
    public void onStaffChat( final UserChatEvent event )
    {
        if ( event.isCancelled() )
        {
            return;
        }
        final User user = event.getUser();

        if ( user.isInStaffChat() )
        {
            if ( user.hasPermission( ConfigFiles.GENERALCOMMANDS.getConfig().getString( "staffchat.permission" ) ) )
            {
                event.setCancelled( true );

                StaffChatCommandCall.sendStaffChatMessage( user, event.getMessage() );
            }
            else
            {
                user.setInStaffChat( false );
            }
        }
    }

    @Event( priority = Priority.LOWEST )
    public void onCharChat( final UserChatEvent event )
    {
        final IConfiguration config = ConfigFiles.GENERALCOMMANDS.getConfig();
        final String detect = config.getString( "staffchat.charchat.detect" );

        if ( !config.getBoolean( "staffchat.enabled" )
            || !config.getBoolean( "staffchat.charchat.enabled" )
            || !event.getMessage().startsWith( detect ) )
        {
            return;
        }
        final User user = event.getUser();
        final String permission = config.getString( "staffchat.permission" );
        if ( !user.hasPermission( permission ) || user.isInStaffChat() )
        {
            return;
        }
        final String message = event.getMessage().substring( detect.length() );

        StaffChatCommandCall.sendStaffChatMessage( user, message );
        event.setCancelled( true );
    }
}
