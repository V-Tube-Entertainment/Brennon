package com.gizmo.brennon.core.protocolize.guis.friends.friendrequests.request;

import com.gizmo.brennon.core.api.friends.FriendRequestType;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;

public class FriendRequestsGuiConfig extends GuiConfig
{

    public FriendRequestsGuiConfig( final FriendRequestType type )
    {
        super( "/configurations/gui/friends/" + type.toString().toLowerCase() + "friendrequests.yml", FriendRequestGuiConfigItem.class );
    }

}
