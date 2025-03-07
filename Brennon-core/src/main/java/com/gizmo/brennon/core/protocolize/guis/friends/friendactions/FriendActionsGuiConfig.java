package com.gizmo.brennon.core.protocolize.guis.friends.friendactions;

import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;
import com.gizmo.brennon.core.protocolize.guis.friends.friend.FriendGuiConfigItem;

public class FriendActionsGuiConfig extends GuiConfig
{
    public FriendActionsGuiConfig()
    {
        super( "/configurations/gui/friends/friendactions.yml", FriendGuiConfigItem.class );
    }
}
