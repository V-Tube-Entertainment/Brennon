package com.gizmo.brennon.core.commands.general.spy;

import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.user.interfaces.User;

import java.util.List;

public class SocialSpyCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( !user.isSocialSpy() )
        {
            user.sendLangMessage( "general-commands.socialspy.enabled" );
            user.setSocialSpy( true );
        }
        else
        {
            user.sendLangMessage( "general-commands.socialspy.disabled" );
            user.setSocialSpy( false );
        }
    }

    @Override
    public String getDescription()
    {
        return "Toggles social spy for the current session. This will allow you to see all private messages that are being sent.";
    }

    @Override
    public String getUsage()
    {
        return "/socialspy";
    }
}
