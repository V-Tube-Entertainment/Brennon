package com.gizmo.brennon.core.protocolize.guis.friends.friendrequests.request;

import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItem;
import dev.endoy.configuration.api.ISection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode( callSuper = true )
public class FriendRequestGuiConfigItem extends GuiConfigItem
{

    private final boolean requestItem;

    public FriendRequestGuiConfigItem( final GuiConfig guiConfig, final ISection section )
    {
        super( guiConfig, section );

        this.requestItem = section.exists( "request-slots" ) && section.getBoolean( "request-slots" );
    }
}
