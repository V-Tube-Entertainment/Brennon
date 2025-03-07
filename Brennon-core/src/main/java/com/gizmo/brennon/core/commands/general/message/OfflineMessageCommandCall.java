package com.gizmo.brennon.core.commands.general.message;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.Command;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.storage.dao.OfflineMessageDao;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class OfflineMessageCommandCall implements CommandCall
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
            user.sendLangMessage( "general-commands.offlinemessage.usage" );
            return;
        }
        final String targetName = args.get( 0 );
        final String message = String.join( " ", args.subList( 1, args.size() ) );

        if ( BuX.getApi().getPlayerUtils().isOnline( targetName ) )
        {
            final Optional<Command> msgCommand = BuX.getInstance().getCommandManager().findCommandByName( "msg" );

            if ( ConfigFiles.GENERALCOMMANDS.getConfig().getBoolean( "offlinemessage.execute-msg-if-online" ) && msgCommand.isPresent() )
            {
                msgCommand.get().execute( user, args, parameters );
            }
            else
            {
                user.sendLangMessage( "general-commands.offlinemessage.online" );
            }
            return;
        }

        BuX.getApi().getStorageManager().getDao().getOfflineMessageDao().sendOfflineMessage(
            targetName,
            new OfflineMessageDao.OfflineMessage(
                null,
                "general-commands.offlinemessage.message",
                MessagePlaceholders.create()
                    .append( user )
                    .append( "time", Utils.formatDate( new Date(), user.getLanguageConfig().getConfig() ) )
                    .append( "message", message )
            )
        );
        user.sendLangMessage( "general-commands.offlinemessage.sent", MessagePlaceholders.create().append( "user", targetName ) );
    }

    @Override
    public String getDescription()
    {
        return "Sends an offline private message to a user.";
    }

    @Override
    public String getUsage()
    {
        return "/offlinemessage (user) (message)";
    }
}
