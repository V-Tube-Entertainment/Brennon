package com.gizmo.brennon.core.protocolize.guis.friends.friend;

import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;

public class FriendGuiConfig extends GuiConfig
{
    public FriendGuiConfig()
    {
        super( "/configurations/gui/friends/friends.yml", FriendGuiConfigItem.class );
    }
}
