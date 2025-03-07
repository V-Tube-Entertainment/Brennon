package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.UserUnmuteJob;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import com.gizmo.brennon.core.api.user.UserStorageKey;

import java.util.List;

import static com.gizmo.brennon.core.api.storage.dao.PunishmentDao.useServerPunishments;

public class UserUnmuteJobHandler
{

    @JobHandler
    void handleUserUnmuteJob( final UserUnmuteJob job )
    {
        job.getUser().ifPresent( user ->
        {
            if ( !user.getStorage().hasData( UserStorageKey.CURRENT_MUTES ) )
            {
                return;
            }
            final List<PunishmentInfo> mutes = user.getStorage().getData( UserStorageKey.CURRENT_MUTES );

            mutes.removeIf( mute ->
            {
                if ( useServerPunishments() )
                {
                    return mute.getServer().equalsIgnoreCase( job.getServerName() );
                }
                else
                {
                    return true;
                }
            } );
        } );
    }
}
