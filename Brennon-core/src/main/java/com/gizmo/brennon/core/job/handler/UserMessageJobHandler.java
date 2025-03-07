package com.gizmo.brennon.core.job.handler;

import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.job.jobs.UserMessageJob;
import com.gizmo.brennon.core.api.job.management.AbstractJobHandler;
import com.gizmo.brennon.core.api.job.management.JobHandler;

public class UserMessageJobHandler extends AbstractJobHandler
{

    @JobHandler
    void executeUserMessageJob( final UserMessageJob job )
    {
        job.getUser().ifPresent( user ->
        {
            if ( job.getMessage().contains( "\n" ) )
            {
                final String[] lines = job.getMessage().split( "\n" );

                for ( String line : lines )
                {
                    user.sendMessage( line );
                }
            }
            else
            {
                user.sendMessage( job.getMessage() );
            }
        } );
    }

    @JobHandler
    void executeUserLanguageMessageJob( final UserLanguageMessageJob job )
    {
        job.getUser().or( job::getUserByName ).ifPresent( user -> user.sendLangMessage(
            job.getLanguagePath(),
            job.isPrefix(),
            null,
            null,
            job.getPlaceholders()
        ) );
    }
}
