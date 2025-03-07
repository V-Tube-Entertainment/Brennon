package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.UserMuteJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.user.UserStorageKey;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;

public class UserMuteJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeUserMuteJob( final UserMuteJob job )
    {
        job.getUsers().forEach( user -> this.muteUser(
            user,
            job.getLanguagePath(),
            job.getPlaceholders(),
            job.getPunishmentType(),
            job.getReason()
        ) );
    }

    private void muteUser( final User user,
                           final String languagePath,
                           final MessagePlaceholders placeholders,
                           final PunishmentType punishmentType,
                           final String reason )
    {
        List<String> mute = null;
        if ( BuX.getApi().getPunishmentExecutor().isTemplateReason( reason ) )
        {
            mute = BuX.getApi().getPunishmentExecutor().searchTemplate(
                user.getLanguageConfig().getConfig(), punishmentType, reason
            );
        }
        if ( mute == null )
        {
            user.sendLangMessage( languagePath, placeholders );
        }
        else
        {
            mute.forEach( str -> user.sendRawColorMessage( Utils.replacePlaceHolders( user, str, placeholders ) ) );
        }

        // if CURRENT_MUTES key is abscent, the next time a user chats, BuX will fetch the mutes for that user
        user.getStorage().removeData( UserStorageKey.CURRENT_MUTES );
    }
}
