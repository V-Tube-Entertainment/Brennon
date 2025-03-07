package com.gizmo.brennon.core.protocolize.guis.friends.friendrequests;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItem;

public class MainFriendRequestsItemPage extends ItemPage
{

    public MainFriendRequestsItemPage( final User user, final MainFriendRequestsGuiConfig guiConfig )
    {
        super( guiConfig.getRows() * 9 );

        for ( GuiConfigItem item : guiConfig.getItems() )
        {
            for ( int slot : item.getSlots() )
            {
                super.setItem( slot, this.getGuiItem( user, item ) );
            }
        }
    }
}
