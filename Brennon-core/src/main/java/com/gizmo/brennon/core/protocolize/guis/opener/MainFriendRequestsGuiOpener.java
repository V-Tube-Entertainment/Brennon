package com.gizmo.brennon.core.protocolize.guis.opener;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import com.gizmo.brennon.core.protocolize.gui.GuiOpener;
import com.gizmo.brennon.core.protocolize.guis.DefaultGui;
import com.gizmo.brennon.core.protocolize.guis.friends.friendrequests.MainFriendRequestsGuiConfig;
import com.gizmo.brennon.core.protocolize.guis.friends.friendrequests.MainFriendRequestsGuiItemProvider;

public class MainFriendRequestsGuiOpener extends GuiOpener
{
    public MainFriendRequestsGuiOpener()
    {
        super( "friendrequests" );
    }

    @Override
    public void openGui( final User user, final String[] args )
    {
        final MainFriendRequestsGuiConfig config = DefaultGui.FRIENDREQUESTS.getConfig();
        final Gui gui = Gui.builder()
            .itemProvider( new MainFriendRequestsGuiItemProvider( user, config ) )
            .rows( config.getRows() )
            .title( config.getTitle() )
            .user( user )
            .build();

        gui.open();
    }
}
