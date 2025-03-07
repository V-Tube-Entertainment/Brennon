package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.party.*;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.gizmo.brennon.core.api.utils.text.PageUtils;
import com.gizmo.brennon.core.api.utils.text.PageUtils.PageMessageInfo;
import com.gizmo.brennon.core.api.utils.text.PageUtils.PageResponseHandler;

import java.util.List;
import java.util.Optional;

public class PartyListSubCommandCall implements CommandCall
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
            final String type = args.get( 0 );

            if ( type.equalsIgnoreCase( "requests" ) )
            {
                this.sendJoinRequestsList( user, party, args );
                return;
            }
            else if ( type.equalsIgnoreCase( "invites" ) )
            {
                this.sendInvitesList( user, party, args );
                return;
            }
        }
        this.sendMembersList( user, party, args );
    }

    @Override
    public String getDescription()
    {
        return "Shows a list of members, invites or requests of your current party.";
    }

    @Override
    public String getUsage()
    {
        return "/party list (members / invites / requests) [page]";
    }

    private void sendMembersList( final User user, final Party party, final List<String> args )
    {
        PageUtils.sendPagedList( user, party.getPartyMembers(), args.size() == 2 ? args.get( 1 ) : "1", 10, new PageResponseHandler<>()
        {

            @Override
            public PageMessageInfo getEmptyListMessage()
            {
                return new PageMessageInfo( "party.list.members.empty" );
            }

            @Override
            public PageMessageInfo getHeaderMessage()
            {
                return new PageMessageInfo( "party.list.members.header" );
            }

            @Override
            public PageMessageInfo getItemMessage( final PartyMember member )
            {
                return new PageMessageInfo(
                    "party.list.members.item",
                    MessagePlaceholders.create()
                        .append( "user", member.getUserName() )
                        .append( "role", PartyUtils.getRoleName( party, member.getUuid(), user.getLanguageConfig() ) )
                        .append( "joinedAt", Utils.formatDate( member.getJoinedAt(), user.getLanguageConfig().getConfig() ) )
                );
            }

            @Override
            public PageMessageInfo getFooterMessage()
            {
                return new PageMessageInfo( "party.list.members.footer" );
            }

            @Override
            public PageMessageInfo getInvalidPageMessage()
            {
                return new PageMessageInfo( "party.list.members.wrong-page" );
            }
        } );
    }

    private void sendInvitesList( final User user, final Party party, final List<String> args )
    {
        PageUtils.sendPagedList( user, party.getSentInvites(), args.size() == 2 ? args.get( 1 ) : "1", 10, new PageResponseHandler<>()
        {

            @Override
            public PageMessageInfo getEmptyListMessage()
            {
                return new PageMessageInfo( "party.list.invites.empty" );
            }

            @Override
            public PageMessageInfo getHeaderMessage()
            {
                return new PageMessageInfo( "party.list.invites.header" );
            }

            @Override
            public PageMessageInfo getItemMessage( final PartyInvite invite )
            {
                return new PageMessageInfo(
                    "party.list.invites.item",
                    MessagePlaceholders.create()
                        .append( "user", invite.getInviteeName() )
                        .append( "invitedAt", Utils.formatDate( invite.getInvitedAt(), user.getLanguageConfig().getConfig() ) )
                );
            }

            @Override
            public PageMessageInfo getFooterMessage()
            {
                return new PageMessageInfo( "party.list.invites.footer" );
            }

            @Override
            public PageMessageInfo getInvalidPageMessage()
            {
                return new PageMessageInfo( "party.list.invites.wrong-page" );
            }
        } );
    }

    private void sendJoinRequestsList( final User user, final Party party, final List<String> args )
    {
        PageUtils.sendPagedList( user, party.getJoinRequests(), args.size() == 2 ? args.get( 1 ) : "1", 10, new PageResponseHandler<>()
        {

            @Override
            public PageMessageInfo getEmptyListMessage()
            {
                return new PageMessageInfo( "party.list.requests.empty" );
            }

            @Override
            public PageMessageInfo getHeaderMessage()
            {
                return new PageMessageInfo( "party.list.requests.header" );
            }

            @Override
            public PageMessageInfo getItemMessage( final PartyJoinRequest request )
            {
                return new PageMessageInfo(
                    "party.list.requests.item",
                    MessagePlaceholders.create()
                        .append( "user", request.getRequesterName() )
                        .append( "requestedAt", Utils.formatDate( request.getRequestedAt(), user.getLanguageConfig().getConfig() ) )
                );
            }

            @Override
            public PageMessageInfo getFooterMessage()
            {
                return new PageMessageInfo( "party.list.requests.footer" );
            }

            @Override
            public PageMessageInfo getInvalidPageMessage()
            {
                return new PageMessageInfo( "party.list.requests.wrong-page" );
            }
        } );
    }
}
