package com.gizmo.brennon.core.protocolize.guis.custom;

import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItem;

public class CustomItemPage extends ItemPage
{

    public CustomItemPage( final User user,
                           final GuiConfig guiConfig )
    {
        super( guiConfig.getRows() * 9 );

        for ( GuiConfigItem item : guiConfig.getItems() )
        {
            for ( int slot : item.getSlots() )
            {
                super.setItem(
                    slot,
                    this.getGuiItem(
                        item.getAction(),
                        item.getRightAction(),
                        item.getItem().buildItem( user )
                    )
                );
            }
        }
    }
}
