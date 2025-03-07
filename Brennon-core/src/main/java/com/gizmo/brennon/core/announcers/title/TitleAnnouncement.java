package com.gizmo.brennon.core.announcers.title;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.announcer.Announcement;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import dev.endoy.configuration.api.IConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.stream.Stream;

@Getter
@Setter
@ToString
@EqualsAndHashCode( callSuper = false )
public class TitleAnnouncement extends Announcement
{

    private boolean language;
    private TitleMessage titleMessage;

    public TitleAnnouncement( final boolean language,
                              final TitleMessage titleMessage,
                              final ServerGroup serverGroup,
                              final String receivePermission )
    {
        super( serverGroup, receivePermission );

        this.language = language;
        this.titleMessage = titleMessage;
    }

    public void send()
    {
        if ( serverGroup.isGlobal() )
        {
            send( filter( BuX.getApi().getUsers().stream() ) );
        }
        else
        {
            serverGroup.getServers().forEach( server -> send( filter( server.getUsers().stream() ) ) );
        }
    }

    private void send( Stream<User> stream )
    {
        stream.forEach( user ->
        {
            final IConfiguration config = user.getLanguageConfig().getConfig();

            user.sendTitle(
                language && config.exists( titleMessage.getTitle() )
                    ? config.getString( titleMessage.getTitle() )
                    : titleMessage.getTitle(),
                language && config.exists( titleMessage.getSubtitle() )
                    ? config.getString( titleMessage.getSubtitle() )
                    : titleMessage.getSubtitle(),
                titleMessage.getFadeIn(),
                titleMessage.getStay(),
                titleMessage.getFadeOut()
            );
        } );
    }
}