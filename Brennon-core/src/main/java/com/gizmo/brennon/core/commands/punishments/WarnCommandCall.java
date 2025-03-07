package com.gizmo.brennon.core.commands.punishments;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.UserWarnJob;
import com.gizmo.brennon.core.api.punishments.IPunishmentHelper;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

import java.util.List;

public class WarnCommandCall extends PunishmentCommand
{

    public WarnCommandCall()
    {
        super( "punishments.warn", false );
    }

    @Override
    public void onPunishmentExecute( final User user,
                                     final List<String> args,
                                     final List<String> parameters,
                                     final PunishmentArgs punishmentArgs )
    {
        final String reason = punishmentArgs.getReason();
        final UserStorage storage = punishmentArgs.getStorage();

        if ( !BuX.getApi().getPlayerUtils().isOnline( punishmentArgs.getPlayer() ) )
        {
            user.sendLangMessage( "offline" );
            return;
        }

        if ( punishmentArgs.launchEvent( PunishmentType.WARN ) )
        {
            return;
        }
        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        dao().getPunishmentDao().getKickAndWarnDao().insertWarn(
            storage.getUuid(),
            storage.getUserName(),
            storage.getIp(),
            reason,
            punishmentArgs.getServerOrAll(),
            user.getName()
        ).thenAccept( info ->
        {
            BuX.getInstance().getJobManager().executeJob( new UserWarnJob(
                storage.getUuid(),
                storage.getUserName(),
                info
            ) );
            user.sendLangMessage( "punishments.warn.executed", executor.getPlaceHolders( info ) );

            if ( !parameters.contains( "-s" ) )
            {
                if ( parameters.contains( "-nbp" ) )
                {
                    BuX.getApi().langBroadcast(
                        "punishments.warn.broadcast",
                        executor.getPlaceHolders( info )
                    );
                }
                else
                {
                    BuX.getApi().langPermissionBroadcast(
                        "punishments.warn.broadcast",
                        ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.warn.broadcast" ),
                        executor.getPlaceHolders( info )
                    );
                }
            }

            punishmentArgs.launchPunishmentFinishEvent( PunishmentType.WARN );
        } );
    }

    @Override
    public String getDescription()
    {
        return "Warns a user for a given reason.";
    }

    @Override
    public String getUsage()
    {
        return "/warn (user) <server> (reason)";
    }
}