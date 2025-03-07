package com.gizmo.brennon.core.protocolize.guis.parties;

import com.gizmo.brennon.core.protocolize.gui.config.GuiConfig;

public class PartyGuiConfig extends GuiConfig
{
    public PartyGuiConfig()
    {
        super( "/configurations/gui/party/party.yml", PartyGuiConfigItem.class );
    }
}
