package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.UserWarnJob;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.punishments.PunishmentType;

import java.util.List;

public class UserWarnJobHandler
{

    @JobHandler
    void handleUserWarnJob( final UserWarnJob job )
    {
        job.getUser().ifPresent( user ->
        {
            List<String> warn = null;
            if ( BuX.getApi().getPunishmentExecutor().isTemplateReason( job.getInfo().getReason() ) )
            {
                warn = BuX.getApi().getPunishmentExecutor().searchTemplate(
                    user.getLanguageConfig().getConfig(), PunishmentType.WARN, job.getInfo().getReason()
                );
            }
            if ( warn == null )
            {
                warn = user.getLanguageConfig().getConfig().getStringList( "punishments.warn.onwarn" );
            }

            warn.forEach( str -> user.sendRawColorMessage( BuX.getApi().getPunishmentExecutor().setPlaceHolders( str, job.getInfo() ) ) );
        } );
    }
}
