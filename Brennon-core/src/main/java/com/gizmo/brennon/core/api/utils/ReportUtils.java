package com.gizmo.brennon.core.api.utils;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import dev.endoy.configuration.api.IConfiguration;

import java.util.UUID;

public class ReportUtils
{

    public static void handleReportsFor( final String accepter, final UUID uuid, final PunishmentType type )
    {
        final IConfiguration config = ConfigFiles.GENERALCOMMANDS.getConfig();

        if ( !config.getBoolean( "report.enabled" ) )
        {
            return;
        }

        if ( !config.getStringList( "report.accept_on_punishment" ).contains( type.toString() ) )
        {
            return;
        }

        BuX.getApi().getStorageManager().getDao().getReportsDao().getReports( uuid )
            .thenAccept( reports -> reports.forEach( report -> report.accept( accepter ) ) );
    }
}
