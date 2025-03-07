package com.gizmo.brennon.core.protocolize.gui.item;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import dev.simplix.protocolize.api.inventory.InventoryClick;
import dev.simplix.protocolize.api.item.ItemStack;

public interface GuiItem
{

    GuiItem copy();

    ItemStack asItemStack();

    void onClick( final Gui gui, final User user, final InventoryClick event );

}
