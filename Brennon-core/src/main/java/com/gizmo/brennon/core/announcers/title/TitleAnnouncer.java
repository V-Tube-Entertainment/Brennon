package com.gizmo.brennon.core.announcers.title;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.announcer.AnnouncementType;
import com.gizmo.brennon.core.api.announcer.Announcer;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import dev.endoy.configuration.api.ISection;

import java.util.Optional;

public class TitleAnnouncer extends Announcer
{

    public TitleAnnouncer()
    {
        super( AnnouncementType.TITLE );
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
            final String permission = section.getString( "permission" );
            final boolean language = section.getBoolean( "language" );

            addAnnouncement( new TitleAnnouncement( language, new TitleMessage( section ), optionalGroup.get(), permission ) );
        }
    }
}