package com.gizmo.brennon.core.announcers.chat;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.announcer.AnnouncementType;
import com.gizmo.brennon.core.api.announcer.Announcer;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import dev.endoy.configuration.api.ISection;

import java.util.List;
import java.util.Optional;

public class ChatAnnouncer extends Announcer
{

    public ChatAnnouncer()
    {
        super( AnnouncementType.CHAT );
    }

    @Override
    public void loadAnnouncements()
    {
        for ( ISection section : configuration.getSectionList( "announcements" ) )
        {
            final Optional<ServerGroup> optionalGroup = ConfigFiles.SERVERGROUPS.getServer( section.getString( "server" ) );

            if ( optionalGroup.isEmpty() )
            {
                BuX.getLogger().warning(
                    "Could not find a servergroup or -name for " + section.getString( "server" ) + "!"
                );
                return;
            }

            final String messagesKey = "messages";
            final boolean usePrefix = section.getBoolean( "use-prefix" );
            final String permission = section.getString( "permission" );

            if ( section.isList( messagesKey ) )
            {
                List<String> messages = section.getStringList( messagesKey );

                addAnnouncement( new ChatAnnouncement( usePrefix, messages, optionalGroup.get(), permission ) );
            }
            else
            {
                String messagePath = section.getString( messagesKey );

                addAnnouncement( new ChatAnnouncement( usePrefix, messagePath, optionalGroup.get(), permission ) );
            }
        }
    }
}