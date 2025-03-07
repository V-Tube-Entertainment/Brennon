package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.UserLanguageMessageJob;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.party.PartyUtils;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRolePermission;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.Optional;

public class PartyKickSubCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() != 1 )
        {
            user.sendLangMessage( "party.kick.usage" );
            return;
        }
        final Optional<Party> optionalParty = BuX.getInstance().getPartyManager().getCurrentPartyFor( user.getName() );

        if ( optionalParty.isEmpty() )
        {
            user.sendLangMessage( "party.not-in-party" );
            return;
        }
        final Party party = optionalParty.get();

        if ( !PartyUtils.hasPermission( party, user, PartyRolePermission.INVITE ) )
        {
            user.sendLangMessage( "party.kick.not-allowed" );
            return;
        }
        final PartyMember currentMember = party.getMemberByUuid( user.getUuid() ).orElse( null );
        final String targetName = args.get( 0 );

        party.getPartyMembers()
            .stream()
            .filter( m -> m.getUserName().equalsIgnoreCase( targetName ) || m.getNickName().equalsIgnoreCase( targetName ) )
            .findFirst()
            .ifPresentOrElse( member ->
            {
                if ( !party.isOwner( currentMember.getUuid() )
                    && ( party.isOwner( member.getUuid() ) || currentMember.getPartyRolePriority() <= member.getPartyRolePriority() ) )
                {
                    user.sendLangMessage(
                        "party.kick.cannot-kick",
                        MessagePlaceholders.create()
                            .append( "user", member.getUserName() )
                    );
                    return;
                }

                BuX.getInstance().getPartyManager().removeMemberFromParty( party, member );

                user.sendLangMessage(
                    "party.kick.kick",
                    MessagePlaceholders.create()
                        .append( "kickedUser", member.getUserName() )
                );

                BuX.getInstance().getPartyManager().languageBroadcastToParty(
                    party,
                    "party.kick.kicked-broadcast",
                    MessagePlaceholders.create()
                        .append( "kickedUser", member.getUserName() )
                        .append( "user", user.getName() )
                );

                BuX.getInstance().getJobManager().executeJob( new UserLanguageMessageJob(
                    member.getUuid(),
                    "party.kick.kicked",
                    MessagePlaceholders.create()
                        .append( "user", user.getName() )
                ) );
            }, () -> user.sendLangMessage( "party.kick.not-in-party" ) );
    }

    @Override
    public String getDescription()
    {
        return "Kicks a member from the party.";
    }

    @Override
    public String getUsage()
    {
        return "/party kick (user)";
    }
}
