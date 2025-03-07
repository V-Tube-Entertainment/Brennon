package com.gizmo.brennon.core.api.user.interfaces;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.bossbar.IBossBar;
import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.friends.FriendSettings;
import com.gizmo.brennon.core.api.language.Language;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.UserCooldowns;
import com.gizmo.brennon.core.api.user.UserSettings;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.utils.MessageUtils;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.Version;
import com.gizmo.brennon.core.api.utils.placeholders.HasMessagePlaceholders;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class ConsoleUser implements User
{

    private static final String NOT_SUPPORTED = "The console does not support this operation!";
    private final UserStorage storage = new UserStorage();
    private final UserCooldowns cooldowns = new UserCooldowns();
    @Getter
    private final List<FriendData> friends = Lists.newArrayList();
    private final UUID uuid = UUID.randomUUID();
    private final UserSettings userSettings = new UserSettings( uuid, new ArrayList<>() );
    @Getter
    private final List<IBossBar> activeBossBars = Collections.synchronizedList( new ArrayList<>() );
    @Getter
    @Setter
    private boolean socialSpy;
    @Getter
    @Setter
    private boolean commandSpy;
    @Getter
    private boolean loaded = true;

    @Override
    public void load( Object playerInstance )
    {
        // do nothing
    }

    @Override
    public void unload()
    {
        // do nothing
    }

    @Override
    public void save( final boolean logout )
    {
        // do nothing
    }

    @Override
    public UserStorage getStorage()
    {
        return storage;
    }

    @Override
    public UserCooldowns getCooldowns()
    {
        return cooldowns;
    }

    @Override
    public String getIp()
    {
        return "127.0.0.1";
    }

    @Override
    public Language getLanguage()
    {
        return BuX.getApi().getLanguageManager().getDefaultLanguage();
    }

    @Override
    public void setLanguage( Language language )
    {
        // do nothing
    }

    @Override
    public void sendRawMessage( String message )
    {
        sendMessage( MessageUtils.fromTextNoColors( message ) );
    }

    @Override
    public void sendRawColorMessage( String message )
    {
        if ( message.isEmpty() )
        {
            return;
        }
        sendMessage( Utils.format( message ) );
    }

    @Override
    public void sendMessage( String message )
    {
        if ( message.isEmpty() )
        {
            return;
        }
        sendMessage( getLanguageConfig().getConfig().getString( "prefix" ), message );
    }

    @Override
    public void sendMessage( String prefix, String message )
    {
        sendMessage( Utils.format( prefix + message ) );
    }

    @Override
    public void kick( String reason )
    {
        // do nothing
    }

    @Override
    public void langKick( String path, HasMessagePlaceholders placeholders )
    {
        // do nothing
    }

    @Override
    public void forceKick( String reason )
    {
        // do nothing
    }

    @Override
    public String getName()
    {
        return "CONSOLE";
    }

    @Override
    public UUID getUuid()
    {
        return uuid;
    }

    @Override
    public void sendNoPermMessage()
    {
        sendLangMessage( "no-permission" );
    }

    @Override
    public int getPing()
    {
        return 0;
    }

    @Override
    public boolean isConsole()
    {
        return true;
    }

    @Override
    public String getServerName()
    {
        return "PROXY";
    }

    @Override
    public boolean isInStaffChat()
    {
        return false;
    }

    @Override
    public void setInStaffChat( boolean staffchat )
    {
        // do nothing
    }

    @Override
    public void sendToServer( IProxyServer proxyServer )
    {
        // do nothing
    }

    @Override
    public Version getVersion()
    {
        return Version.latest();
    }

    @Override
    public FriendSettings getFriendSettings()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }


    @Override
    public void sendOfflineMessages()
    {
        // do nothing
    }

    @Override
    public void sendActionBar( String actionbar )
    {
        // do nothing
    }

    @Override
    public void sendTitle( String title, String subtitle, int fadein, int stay, int fadeout )
    {
        // do nothing
    }

    @Override
    public boolean isMsgToggled()
    {
        return false;
    }

    @Override
    public void setMsgToggled( boolean status )
    {
        // do nothing
    }

    @Override
    public void sendPacket( final Object packet )
    {
        // do nothing
    }

    @Override
    public void setTabHeader( Component header, Component footer )
    {
        // do nothing
    }

    @Override
    public String getJoinedHost()
    {
        throw new UnsupportedOperationException( NOT_SUPPORTED );
    }

    @Override
    public boolean isVanished()
    {
        return false;
    }

    @Override
    public void setVanished( boolean vanished )
    {
        // do nothing
    }

    @Override
    public String getGroup()
    {
        return "";
    }

    @Override
    public String getLanguageTagShort()
    {
        return "en";
    }

    @Override
    public String getLanguageTagLong()
    {
        return "en_US";
    }

    @Override
    public Object getPlayerObject()
    {
        return null;
    }

    @Override
    public UserSettings getSettings()
    {
        return userSettings;
    }

}
