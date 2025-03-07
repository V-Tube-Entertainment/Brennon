package com.gizmo.brennon.core.protocolize.guis.opener;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.Gui;
import com.gizmo.brennon.core.protocolize.gui.GuiOpener;
import com.gizmo.brennon.core.protocolize.guis.DefaultGui;
import com.gizmo.brennon.core.protocolize.guis.parties.PartyGuiConfig;
import com.gizmo.brennon.core.protocolize.guis.parties.PartyGuiItemProvider;

import java.util.List;
import java.util.Optional;

public class PartyGuiOpener extends GuiOpener
{

    public PartyGuiOpener()
    {
        super( "party" );
    }

    @Override
    public void openGui( final User user, final String[] args )
    {
        if ( !BuX.getInstance().isPartyManagerEnabled() )
        {
            user.sendLangMessage( "party.not-in-party" );
            return;
        }
        final Optional<Party> optionalParty = BuX.getInstance().getPartyManager().getCurrentPartyFor( user.getName() );

        if ( optionalParty.isEmpty() )
        {
            user.sendLangMessage( "party.not-in-party" );
            return;
        }
        final Party party = optionalParty.get();
        final List<PartyMember> partyMembers = party.getPartyMembers();
        final PartyGuiConfig config = DefaultGui.PARTY.getConfig();
        final Gui gui = Gui.builder()
            .itemProvider( new PartyGuiItemProvider( user, config, party, partyMembers ) )
            .rows( config.getRows() )
            .title( config.getTitle() )
            .user( user )
            .build();

        gui.open();
    }
}
