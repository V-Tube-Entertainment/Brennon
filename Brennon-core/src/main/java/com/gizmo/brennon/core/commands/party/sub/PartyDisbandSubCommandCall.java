package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class PartyDisbandSubCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        final Optional<Party> optionalParty = BuX.getInstance().getPartyManager().getCurrentPartyFor( user.getName() );

        if ( optionalParty.isEmpty() )
        {
            user.sendLangMessage( "party.not-in-party" );
            return;
        }
        final Party party = optionalParty.get();

        if ( !party.isOwner( user.getUuid() ) )
        {
            user.sendLangMessage( "party.disband.not-allowed" );
            return;
        }

        BuX.getInstance().getPartyManager().languageBroadcastToParty(
            party,
            "party.disband.broadcast",
            MessagePlaceholders.create()
                .append( "user", user.getName() )
        );

        BuX.getInstance().getPartyManager().removeParty( party );
        user.sendLangMessage( "party.disband.disbanded" );
    }

    @Override
    public String getDescription()
    {
        return "Disbands the party.";
    }

    @Override
    public String getUsage()
    {
        return "/party disband";
    }
}
