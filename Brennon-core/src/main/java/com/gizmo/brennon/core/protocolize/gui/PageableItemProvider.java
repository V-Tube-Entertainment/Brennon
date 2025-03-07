package com.gizmo.brennon.core.protocolize.gui;

import com.gizmo.brennon.core.protocolize.gui.item.GuiItem;

import java.util.Optional;

public interface PageableItemProvider
{

    default Optional<GuiItem> getItemAtSlot( final int rawSlot )
    {
        return getItemAtSlot( 1, rawSlot );
    }

    default ItemPage getItemContents()
    {
        return getItemContents( 1 );
    }

    Optional<GuiItem> getItemAtSlot( int page, int rawSlot );

    ItemPage getItemContents( int page );

    int getPageAmount();
}
