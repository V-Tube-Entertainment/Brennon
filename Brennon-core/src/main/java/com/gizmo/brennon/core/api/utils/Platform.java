package com.gizmo.brennon.core.api.utils;

import com.gizmo.brennon.core.BuX;

public enum Platform
{

    BUNGEECORD,
    VELOCITYPOWERED,
    SPIGOT,
    SPRING;

    private static Platform CURRENT_PLATFORM = null;

    public static Platform getCurrentPlatform()
    {
        if ( CURRENT_PLATFORM == null && BuX.getInstance() != null )
        {
            CURRENT_PLATFORM = BuX.getInstance().getPlatform();
        }

        return CURRENT_PLATFORM;
    }

    public static void setCurrentPlatform( Platform currentPlatform )
    {
        CURRENT_PLATFORM = currentPlatform;
    }
}
