package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.UserKickJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

public class UserKickJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeUserKickJob( final UserKickJob job )
    {
        job.getUsers().forEach( user -> this.kickUser(
            user,
            job.getLanguagePath(),
            job.getPlaceholders(),
            job.getPunishmentType(),
            job.getReason()
        ) );
    }

    private void kickUser( final User user,
                           final String languagePath,
                           final MessagePlaceholders placeholders,
                           final PunishmentType punishmentType,
                           final String reason )
    {
        String kick = null;
        if ( BuX.getApi().getPunishmentExecutor().isTemplateReason( reason ) )
        {
            kick = Utils.formatList( BuX.getApi().getPunishmentExecutor().searchTemplate(
                user.getLanguageConfig().getConfig(),
                punishmentType,
                reason
            ), "\n" );
        }
        if ( kick == null )
        {
            kick = Utils.formatList(
                user.getLanguageConfig().getConfig().getStringList( languagePath ),
                "\n"
            );
        }
        kick = Utils.replacePlaceHolders( user, kick, placeholders );
        user.kick( kick );
    }
}
