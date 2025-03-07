package com.gizmo.brennon.core.protocolize.guis.opener;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.friends.FriendRequestType;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import com.gizmo.brennon.core.protocolize.gui.GuiOpener;
import com.gizmo.brennon.core.protocolize.guis.DefaultGui;
import com.gizmo.brennon.core.protocolize.guis.friends.friendrequests.request.FriendRequestsGuiConfig;
import com.gizmo.brennon.core.protocolize.guis.friends.friendrequests.request.FriendRequestsGuiItemProvider;

public class IncomingFriendRequestsGuiOpener extends GuiOpener
{

    public IncomingFriendRequestsGuiOpener()
    {
        super( "incomingfriendrequests" );
    }

    @Override
    public void openGui( final User user, final String[] args )
    {
        BuX.getApi().getStorageManager().getDao().getFriendsDao().getIncomingFriendRequests( user.getUuid() ).thenAccept( incomingRequests ->
        {
            final FriendRequestsGuiConfig config = DefaultGui.INCOMINGFRIENDREQUESTS.getConfig();
            final Gui gui = Gui.builder()
                .itemProvider( new FriendRequestsGuiItemProvider( user, FriendRequestType.INCOMING, config, incomingRequests ) )
                .rows( config.getRows() )
                .title( config.getTitle() )
                .user( user )
                .build();

            gui.open();
        } );
    }
}
