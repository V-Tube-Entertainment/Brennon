package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class PartyChatSubCommandCall implements CommandCall
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

        if ( !args.isEmpty() )
        {
            BuX.getInstance().getPartyManager().languageBroadcastToParty(
                party,
                "party.chat.format",
                MessagePlaceholders.create()
                    .append( "user", user.getName() )
                    .append( "message", String.join( " ", args ) )
            );
        }
        else
        {
            party.getPartyMembers()
                .stream()
                .filter( m -> m.getUuid().equals( user.getUuid() ) )
                .findFirst()
                .ifPresentOrElse( member ->
                {
                    final boolean chatMode = !member.isChat();
                    BuX.getInstance().getPartyManager().setChatMode( party, member, chatMode );

                    user.sendLangMessage( "party.chat." + ( chatMode ? "enabled" : "disabled" ) );
                }, () ->
                {
                    user.sendLangMessage( "party.not-in-party" );
                } );

        }
    }

    @Override
    public String getDescription()
    {
        return "Toggles party chat mode or sends a chat message to the party.";
    }

    @Override
    public String getUsage()
    {
        return "/party chat [message]";
    }
}
