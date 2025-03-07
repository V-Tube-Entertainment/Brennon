package com.gizmo.brennon.core.placeholders;

import com.gizmo.brennon.core.api.placeholder.PlaceHolderAPI;
import com.gizmo.brennon.core.api.placeholder.PlaceHolderPack;
import com.gizmo.brennon.core.api.placeholder.event.InputPlaceHolderEvent;
import com.gizmo.brennon.core.api.placeholder.event.handler.InputPlaceHolderEventHandler;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import dev.endoy.configuration.api.IConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class InputPlaceHolders implements PlaceHolderPack
{

    @Override
    public void loadPack()
    {
        PlaceHolderAPI.addPlaceHolder( false, "timeleft", new InputPlaceHolderEventHandler()
        {
            @Override
            public String getReplacement( InputPlaceHolderEvent event )
            {
                final IConfiguration configuration = Utils.getLanguageConfiguration( event.getUser() ).getConfig();
                final SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy kk:mm:ss" );

                try
                {
                    final Date parsedDate = dateFormat.parse( event.getArgument() );
                    final Date date = ConfigFiles.CONFIG.isEnabled( "timezone", false )
                        ? Date.from( parsedDate.toInstant().atZone( ZoneId.of( ConfigFiles.CONFIG.getConfig().getString( "timezone.zone" ) ) ).toInstant() )
                        : parsedDate;

                    return Utils.getTimeLeft(
                        configuration.getString( "placeholders.timeleft" ),
                        date
                    );
                }
                catch ( ParseException e )
                {
                    return "";
                }
            }
        } );
        PlaceHolderAPI.addPlaceHolder( false, "getcount", new InputPlaceHolderEventHandler()
        {
            @Override
            public String getReplacement( InputPlaceHolderEvent event )
            {
                final Optional<ServerGroup> serverGroup = ConfigFiles.SERVERGROUPS.getServer( event.getArgument() );

                if ( serverGroup.isEmpty() )
                {
                    return "0";
                }

                return String.valueOf( serverGroup.get().getPlayers() );
            }
        } );
    }
}