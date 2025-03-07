package com.gizmo.brennon.core.protocolize.guis.friends.friend;

import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItem;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItemStack;
import dev.endoy.configuration.api.ISection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode( callSuper = true )
public class FriendGuiConfigItem extends GuiConfigItem
{

    private final boolean friendItem;
    private final GuiConfigItemStack offlineItem;
    private final GuiConfigItemStack onlineItem;

    public FriendGuiConfigItem( final GuiConfig guiConfig, final ISection section )
    {
        super( guiConfig, section );

        this.friendItem = section.exists( "friend-slots" ) && section.getBoolean( "friend-slots" );

        if ( this.friendItem )
        {
            this.offlineItem = section.exists( "offlineitem" ) ? new GuiConfigItemStack( section.getSection( "offlineitem" ) ) : null;
            this.onlineItem = section.exists( "onlineitem" ) ? new GuiConfigItemStack( section.getSection( "onlineitem" ) ) : null;
        }
        else
        {
            this.offlineItem = null;
            this.onlineItem = null;
        }
    }
}
