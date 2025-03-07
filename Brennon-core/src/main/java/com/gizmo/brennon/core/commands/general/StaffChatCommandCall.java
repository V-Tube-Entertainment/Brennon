package com.gizmo.brennon.core.commands.general;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.BroadcastLanguageMessageJob;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.StaffUtils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.other.StaffRankData;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.google.common.base.Strings;

import java.util.List;

public class StaffChatCommandCall implements CommandCall
{

    public static void sendStaffChatMessage( final User user, final String message )
    {
        BuX.getInstance().getJobManager().executeJob(
                new BroadcastLanguageMessageJob(
                        "general-commands.staffchat.format",
                        ConfigFiles.GENERALCOMMANDS.getConfig().getString( "staffchat.permission" ),
                        MessagePlaceholders.create()
                                .append( "user", user.getName() )
                                .append( "user_prefix", StaffUtils.getStaffRankForUser( user ).map( StaffRankData::getDisplay ).orElse( "" ) )
                                .append( "permission_user_prefix", Strings.nullToEmpty( BuX.getInstance().getActivePermissionIntegration().getPrefix( user.getUuid() ) ) )
                                .append( "permission_user_suffix", Strings.nullToEmpty( BuX.getInstance().getActivePermissionIntegration().getSuffix( user.getUuid() ) ) )
                                .append( "server", Strings.nullToEmpty( user.getServerName() ) )
                                .append( "message", message ),
                        ConfigFiles.GENERALCOMMANDS.getConfig().getStringList( "staffchat.disabled-servers" )
                )
        );
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        // If amount of arguments > 0, then we should directly send a message in staff chat
        if ( !args.isEmpty() )
        {
            sendStaffChatMessage( user, String.join( " ", args ) );
            return;
        }

        user.setInStaffChat( !user.isInStaffChat() );
        user.sendLangMessage( "general-commands.staffchat." + ( user.isInStaffChat() ? "enabled" : "disabled" ) );
    }

    @Override
    public String getDescription()
    {
        return "Toggles your chat mode into staff chat mode, only people with a given permission can then see your messages.";
    }

    @Override
    public String getUsage()
    {
        return "/staffchat [message]";
    }
}
