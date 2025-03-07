package com.gizmo.brennon.core.protocolize.guis.opener;

import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import com.gizmo.brennon.core.protocolize.gui.GuiOpener;
import com.gizmo.brennon.core.protocolize.guis.DefaultGui;
import com.gizmo.brennon.core.protocolize.guis.friends.friendactions.FriendActionsGuiConfig;
import com.gizmo.brennon.core.protocolize.guis.friends.friendactions.FriendActionsGuiItemProvider;

public class FriendActionsGuiOpener extends GuiOpener
{
    public FriendActionsGuiOpener()
    {
        super( "friendactions" );
    }

    @Override
    public void openGui( final User user, final String[] args )
    {
        final FriendData friendData = user.getFriends()
            .stream()
            .filter( d -> d.getFriend().equalsIgnoreCase( args[0] ) )
            .findFirst()
            .orElse( null );

        if ( friendData != null )
        {
            final FriendActionsGuiConfig config = DefaultGui.FRIENDACTIONS.getConfig();
            final Gui gui = Gui.builder()
                .itemProvider( new FriendActionsGuiItemProvider(
                    user,
                    config,
                    friendData
                ) )
                .rows( config.getRows() )
                .title( config.getTitle().replace( "{friend-name}", friendData.getFriend() ) )
                .user( user )
                .build();

            gui.open();
        }
    }
}
