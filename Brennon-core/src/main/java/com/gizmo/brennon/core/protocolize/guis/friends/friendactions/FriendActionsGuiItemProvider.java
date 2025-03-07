package com.gizmo.brennon.core.protocolize.guis.friends.friendactions;

import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.PageableItemProvider;
import com.gizmo.brennon.core.protocolize.gui.item.GuiItem;

import java.util.Optional;

public class FriendActionsGuiItemProvider implements PageableItemProvider
{

    private final ItemPage page;

    public FriendActionsGuiItemProvider( final User user, final FriendActionsGuiConfig config, final FriendData friendData )
    {
        this.page = new FriendActionsItemPage(
            user,
            config,
            friendData
        );
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
