package com.gizmo.brennon.core.protocolize.guis.opener;

import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import com.gizmo.brennon.core.protocolize.gui.GuiOpener;
import com.gizmo.brennon.core.protocolize.guis.DefaultGui;
import com.gizmo.brennon.core.protocolize.guis.friends.friend.FriendGuiConfig;
import com.gizmo.brennon.core.protocolize.guis.friends.friend.FriendGuiItemProvider;

import java.util.List;

public class FriendGuiOpener extends GuiOpener
{

    public FriendGuiOpener()
    {
        super( "friend" );
    }

    @Override
    public void openGui( final User user, final String[] args )
    {
        final List<FriendData> friends = user.getFriends();
        final FriendGuiConfig config = DefaultGui.FRIEND.getConfig();
        final Gui gui = Gui.builder()
            .itemProvider( new FriendGuiItemProvider( user, config, friends ) )
            .rows( config.getRows() )
            .title( config.getTitle() )
            .user( user )
            .build();

        gui.open();
    }
}
