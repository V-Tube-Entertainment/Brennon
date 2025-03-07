package com.gizmo.brennon.core.protocolize.guis.friends.friendrequests;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.PageableItemProvider;
import com.gizmo.brennon.core.protocolize.gui.item.GuiItem;

import java.util.Optional;

public class MainFriendRequestsGuiItemProvider implements PageableItemProvider
{

    private final ItemPage page;

    public MainFriendRequestsGuiItemProvider( final User user, final MainFriendRequestsGuiConfig config )
    {
        this.page = new MainFriendRequestsItemPage( user, config );
    }

    @Override
    public Optional<GuiItem> getItemAtSlot( final int page, final int rawSlot )
    {
        return this.getItemContents( page ).getItem( rawSlot );
    }

    @Override
    public ItemPage getItemContents( int page )
    {
        return this.page;
    }

    @Override
    public int getPageAmount()
    {
        return 1;
    }
}
