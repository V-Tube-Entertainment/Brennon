package com.gizmo.brennon.core.chat.protections;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.TimeUnit;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.chat.ChatProtection;
import com.gizmo.brennon.core.chat.ChatValidationResult;
import dev.endoy.configuration.api.IConfiguration;

public class SpamChatProtection implements ChatProtection
{

    private boolean enabled;
    private String bypassPermission;
    private TimeUnit delayUnit;
    private int delayTime;

    @Override
    public void reload()
    {
        final IConfiguration config = ConfigFiles.ANTISPAM.getConfig();

        this.enabled = config.getBoolean( "enabled" );
        this.bypassPermission = config.getString( "bypass" );
        this.delayUnit = TimeUnit.valueOf( config.getString( "delay.unit" ).toUpperCase() );
        this.delayTime = config.getInteger( "delay.time" );
    }

    @Override
    public ChatValidationResult validateMessage( final User user, final String message )
    {
        if ( !enabled || user.hasPermission( bypassPermission ) )
        {
            return ChatValidationResult.VALID;
        }
        if ( !user.getCooldowns().canUse( "CHATSPAM" ) )
        {
            return ChatValidationResult.INVALID;
        }
        user.getCooldowns().updateTime( "CHATSPAM", delayUnit, delayTime );
        return ChatValidationResult.VALID;
    }
}
