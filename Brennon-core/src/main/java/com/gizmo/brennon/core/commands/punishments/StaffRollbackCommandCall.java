package com.gizmo.brennon.core.commands.punishments;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StaffRollbackCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() < 2 )
        {
            user.sendLangMessage( "punishments.staffrollback.usage" );
            return;
        }

        final String playerName = args.get( 0 );
        final Date time = new Date( Utils.parseDateDiffInPast( args.get( 1 ) ) );
        final boolean hardRollback = parameters.contains( "-f" );
        final CompletableFuture<Integer> deleteTask;

        if ( hardRollback )
        {
            deleteTask = BuX.getApi().getStorageManager().getDao().getPunishmentDao().hardDeleteSince( playerName, time );
        }
        else
        {
            deleteTask = BuX.getApi().getStorageManager().getDao().getPunishmentDao().softDeleteSince( playerName, user.getName(), time );
        }

        deleteTask.thenAccept( amount -> user.sendLangMessage(
            "punishments.staffrollback." + ( hardRollback ? "hard" : "soft" ) + "-rollback",
            MessagePlaceholders.create()
                .append( "amount", amount )
                .append( "user", playerName )
        ) );
    }

    @Override
    public String getDescription()
    {
        return "Performs a rollback of punishments executed since the given time. If the -f parameter is given, the rollback will be a hard rollback (permanently deleted).";
    }

    @Override
    public String getUsage()
    {
        return "/staffrollback (user) (time) [-f]";
    }
}
