package com.gizmo.brennon.core.protocolize.guis.parties;

import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItem;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItemStack;
import dev.endoy.configuration.api.ISection;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode( callSuper = true )
public class PartyGuiConfigItem extends GuiConfigItem
{

    private final boolean memberItem;
    private final GuiConfigItemStack offlineItem;
    private final GuiConfigItemStack onlineItem;

    public PartyGuiConfigItem( final GuiConfig guiConfig, final ISection section )
    {
        super( guiConfig, section );

        this.memberItem = section.exists( "party-slots" ) && section.getBoolean( "party-slots" );

        if ( this.memberItem )
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
