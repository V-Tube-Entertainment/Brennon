package com.gizmo.brennon.core.commands.party.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.job.jobs.PartyWarpMembersJob;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.party.PartyUtils;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRolePermission;

import java.util.List;
import java.util.Optional;

public class PartyWarpSubCommandCall implements CommandCall
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

        if ( !PartyUtils.hasPermission( party, user, PartyRolePermission.WARP ) )
        {
            user.sendLangMessage( "party.warp.not-allowed" );
            return;
        }

        user.getCurrentServer().ifPresentOrElse( currentServer ->
        {
            final List<PartyMember> partyMembersToWarp;
            if ( args.size() == 1 )
            {
                final String targetName = args.get( 0 );

                partyMembersToWarp = party.getPartyMembers()
                    .stream()
                    .filter( m -> m.getUserName().equalsIgnoreCase( targetName ) || m.getNickName().equalsIgnoreCase( targetName ) )
                    .filter( m ->
                    {
                        final String currentMemberServer = Optional.ofNullable(
                            BuX.getApi().getPlayerUtils().findPlayer( m.getUserName() )
                        ).map( IProxyServer::getName ).orElse( "" );

                        return !currentServer.getName().equals( currentMemberServer )
                            && ConfigFiles.PARTY_CONFIG.canWarpFrom( currentMemberServer );
                    } )
                    .limit( 1 )
                    .toList();
            }
            else
            {
                partyMembersToWarp = party.getPartyMembers()
                    .stream()
                    .filter( m ->
                    {
                        final String currentMemberServer = Optional.ofNullable(
                            BuX.getApi().getPlayerUtils().findPlayer( m.getUserName() )
                        ).map( IProxyServer::getName ).orElse( "" );

                        return !currentServer.getName().equals( currentMemberServer )
                            && ConfigFiles.PARTY_CONFIG.canWarpFrom( currentMemberServer );
                    } )
                    .toList();
            }

            if ( !partyMembersToWarp.isEmpty() )
            {
                BuX.getInstance().getJobManager().executeJob( new PartyWarpMembersJob(
                    party.getUuid(),
                    partyMembersToWarp
                        .stream()
                        .map( PartyMember::getUuid )
                        .toList(),
                    currentServer.getName()
                ) );

                user.sendLangMessage( "party.warp.warping" );
            }
            else
            {
                user.sendLangMessage( "party.warp.nobody-to-warp" );
            }
        }, () -> user.sendLangMessage( "party.warp.failed" ) );
    }

    @Override
    public String getDescription()
    {
        return "Warps all party members to your current server.";
    }

    @Override
    public String getUsage()
    {
        return "/party warp [user]";
    }
}
