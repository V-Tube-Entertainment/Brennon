package com.gizmo.brennon.core.announcers.bossbar;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.announcer.Announcement;
import com.gizmo.brennon.core.api.bossbar.IBossBar;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.TimeUnit;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import com.google.common.collect.Lists;
import dev.endoy.configuration.api.IConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
public class BossBarAnnouncement extends Announcement
{

    private TimeUnit stayUnit;
    private int stayTime;

    private List<BossBarMessage> messages;
    private List<IBossBar> bars = Lists.newArrayList();

    private ScheduledFuture task;

    public BossBarAnnouncement( final List<BossBarMessage> messages,
                                final TimeUnit stayUnit,
                                final int stayTime,
                                final ServerGroup serverGroup,
                                final String receivePermission )
    {
        super( serverGroup, receivePermission );

        this.messages = messages;
        this.stayUnit = stayUnit;
        this.stayTime = stayTime;
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

            messages.forEach( message ->
            {
                final IBossBar bar = BuX.getApi().createBossBar();
                bar.setMessage(
                    Utils.format( user, message.isLanguage()
                        ? config.getString( message.getText() )
                        : message.getText() )
                );
                bar.setColor( message.getColor() );
                bar.setProgress( message.getProgress() );
                bar.setStyle( message.getStyle() );

                bar.addUser( user );
                bars.add( bar );
            } );
        } );
        if ( stayTime > 0 )
        {
            task = BuX.getInstance().getScheduler().runTaskDelayed( stayTime, stayUnit.toJavaTimeUnit(), this::clear );
        }
    }

    @Override
    public void clear()
    {
        bars.forEach( bar ->
        {
            bar.clearUsers();
            bar.unregister();
        } );
        if ( task != null )
        {
            // for if stay > the announcement rotation delay (avoiding useless method calling)
            task.cancel( true );
            task = null;
        }
    }
}