package com.gizmo.brennon.core.protocolize.gui.handlers;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.TriConsumer;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import dev.simplix.protocolize.api.inventory.InventoryClick;

public class NextPageClickHandler implements TriConsumer<Gui, User, InventoryClick>
{

    @Override
    public void accept( final Gui gui, final User user, final InventoryClick event )
    {
        event.cancelled( true );
        gui.setPage( gui.getPage() + 1 );
        gui.refill();
    }
}
