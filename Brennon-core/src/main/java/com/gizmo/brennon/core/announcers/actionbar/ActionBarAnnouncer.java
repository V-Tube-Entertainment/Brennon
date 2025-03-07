package com.gizmo.brennon.core.announcers.actionbar;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.announcer.AnnouncementType;
import com.gizmo.brennon.core.api.announcer.Announcer;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import dev.endoy.configuration.api.ISection;

import java.util.Optional;

public class ActionBarAnnouncer extends Announcer
{

    public ActionBarAnnouncer()
    {
        super( AnnouncementType.ACTIONBAR );
    }

    @Override
    public void loadAnnouncements()
    {
        for ( ISection section : configuration.getSectionList( "announcements" ) )
        {
            final Optional<ServerGroup> optionalGroup = ConfigFiles.SERVERGROUPS.getServer( section.getString( "server" ) );

            if ( optionalGroup.isEmpty() )
            {
                BuX.getLogger().warning( "Could not find a servergroup or -name for " + section.getString( "server" ) + "!" );
                return;
            }

            final boolean useLanguage = section.getBoolean( "use-language" );
            final int time = section.getInteger( "time" );
            final String permission = section.getString( "permission" );

            final String message = section.getString( "message" );

            addAnnouncement( new ActionBarAnnouncement( useLanguage, time, message, optionalGroup.get(), permission ) );
        }
    }
}