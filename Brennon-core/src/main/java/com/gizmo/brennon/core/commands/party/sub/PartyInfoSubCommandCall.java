package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PartyInfoSubCommandCall implements CommandCall
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

        user.sendLangMessage(
            "party.info",
            MessagePlaceholders.create()
                .append( "party-owner", party.getOwner().getNickName() )
                .append( "party-created-at", Utils.formatDate( party.getCreatedAt(), user.getLanguageConfig().getConfig() ) )
                .append( "party-member-count", party.getPartyMembers().size() )
                .append( "party-member-list", party.getPartyMembers().stream().map( PartyMember::getNickName ).collect( Collectors.joining( ", " ) ) )
                .append( "party-invitation-count", party.getSentInvites().size() )
                .append( "party-joinrequest-count", party.getJoinRequests().size() )
        );
    }

    @Override
    public String getDescription()
    {
        return "Shows basic information about your current party.";
    }

    @Override
    public String getUsage()
    {
        return "/party info";
    }
}
