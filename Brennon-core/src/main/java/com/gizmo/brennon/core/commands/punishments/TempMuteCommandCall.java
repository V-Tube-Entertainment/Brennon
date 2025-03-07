package com.gizmo.brennon.core.commands.punishments;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.punishments.IPunishmentHelper;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

import java.util.List;

public class TempMuteCommandCall extends PunishmentCommand
{

    public TempMuteCommandCall()
    {
        super( "punishments.tempmute", true );
    }

    @Override
    public void onPunishmentExecute( final User user, final List<String> args, final List<String> parameters, final PunishmentArgs punishmentArgs )
    {
        final String reason = punishmentArgs.getReason();
        final UserStorage storage = punishmentArgs.getStorage();
        final long time = punishmentArgs.getTime();

        if ( time == 0L )
        {
            user.sendLangMessage( "punishments.tempmute.non-valid" );
            return;
        }
        if ( dao().getPunishmentDao().getMutesDao().isMuted( storage.getUuid(), punishmentArgs.getServerOrAll() ).join() )
        {
            user.sendLangMessage( "punishments.tempmute.already-muted" );
            return;
        }
        if ( punishmentArgs.launchEvent( PunishmentType.TEMPMUTE ) )
        {
            return;
        }
        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        dao().getPunishmentDao().getMutesDao().insertTempMute(
            storage.getUuid(),
            storage.getUserName(),
            storage.getIp(),
            reason,
            punishmentArgs.getServerOrAll(),
            true,
            user.getName(),
            time
        ).thenAccept( info ->
        {
            super.attemptMute( storage, "punishments.tempmute.onmute", info );
            user.sendLangMessage( "punishments.tempmute.executed", executor.getPlaceHolders( info ) );

            if ( !parameters.contains( "-s" ) )
            {
                if ( parameters.contains( "-nbp" ) )
                {
                    BuX.getApi().langBroadcast(
                        "punishments.tempmute.broadcast",
                        executor.getPlaceHolders( info )
                    );
                }
                else
                {
                    BuX.getApi().langPermissionBroadcast(
                        "punishments.tempmute.broadcast",
                        ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.tempmute.broadcast" ),
                        executor.getPlaceHolders( info )
                    );
                }
            }

            punishmentArgs.launchPunishmentFinishEvent( PunishmentType.TEMPMUTE );
        } );
    }

    @Override
    public String getDescription()
    {
        return "Temporarily mutes a given user globally (or given server if per-server punishments are enabled).";
    }

    @Override
    public String getUsage()
    {
        return "/tempmute (user) (time) <server> (reason)";
    }
}