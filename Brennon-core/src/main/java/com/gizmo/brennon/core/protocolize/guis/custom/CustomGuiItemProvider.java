package com.gizmo.brennon.core.protocolize.guis.custom;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.PageableItemProvider;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;
import com.gizmo.brennon.core.protocolize.gui.item.GuiItem;

import java.util.Optional;

public class CustomGuiItemProvider implements PageableItemProvider
{

    private final ItemPage page;

    public CustomGuiItemProvider( final User user, final GuiConfig config )
    {
        this.page = new CustomItemPage( user, config );
    }

    @Override
    public Optional<GuiItem> getItemAtSlot( final int page, final int rawSlot )
    {
        return this.getItemContents( page ).getItem( rawSlot );
    }

    @Override
    public ItemPage getItemContents( final int page )
    {
        return this.page;
    }

    @Override
    public int getPageAmount()
    {
        return 1;
    }
}
