package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class PartyLeaveSubCommandCall implements CommandCall
{

    public static void leaveParty( final Party party, final User user )
    {
        final Optional<PartyMember> optionalPartyMember = party.getPartyMembers()
            .stream()
            .filter( member -> member.getUuid().equals( user.getUuid() ) )
            .findFirst();

        if ( optionalPartyMember.isEmpty() )
        {
            return;
        }
        final PartyMember partyMember = optionalPartyMember.get();

        BuX.getInstance().getPartyManager().removeMemberFromParty(
            party,
            partyMember
        );

        user.sendLangMessage(
            "party.leave.left",
            MessagePlaceholders.create()
                .append( "party-owner", party.getOwner().getUserName() )
        );
        BuX.getInstance().getPartyManager().languageBroadcastToParty(
            party,
            "party.leave.left-broadcast",
            MessagePlaceholders.create()
                .append( "user", user.getName() )
        );

        if ( partyMember.isPartyOwner() && !party.isOwner( user.getUuid() ) )
        {
            BuX.getInstance().getPartyManager().languageBroadcastToParty(
                party,
                "party.leave.owner-left-broadcast",
                MessagePlaceholders.create()
                    .append( "party-old-owner", user.getName() )
                    .append( "{party-owner}", party.getOwner().getUserName() )
            );
        }
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        final Optional<Party> optionalParty = BuX.getInstance().getPartyManager().getCurrentPartyFor( user.getName() );

        if ( optionalParty.isEmpty() )
        {
            user.sendLangMessage( "party.not-in-party" );
            return;
        }

        leaveParty( optionalParty.get(), user );
    }

    @Override
    public String getDescription()
    {
        return "Leaves your current party.";
    }

    @Override
    public String getUsage()
    {
        return "/party leave";
    }
}
