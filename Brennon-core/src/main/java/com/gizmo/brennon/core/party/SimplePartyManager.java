package com.gizmo.brennon.core.party;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.jobs.*;
import com.gizmo.brennon.core.api.party.*;
import com.gizmo.brennon.core.api.party.exceptions.AlreadyInPartyException;
import com.gizmo.brennon.core.api.redis.IRedisDataManager;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.TimeUnit;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRole;
import com.gizmo.brennon.core.api.utils.placeholders.HasMessagePlaceholders;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.google.common.collect.Lists;

import java.util.*;

public class SimplePartyManager implements PartyManager
{

    private final List<Party> parties;

    public SimplePartyManager()
    {
        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            parties = Lists.newCopyOnWriteArrayList( BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().getAllParties() );
        }
        else
        {
            parties = Lists.newCopyOnWriteArrayList( new ArrayList<>() );
        }

        this.startPartyCleanupTask();
    }

    @Override
    public Party createParty( final User leader ) throws AlreadyInPartyException
    {
        if ( getCurrentPartyFor( leader.getName() ).isPresent() )
        {
            throw new AlreadyInPartyException();
        }

        final Party party = new Party( new Date(), this.getPartyLimit( leader ) );
        party.getPartyMembers().add( new PartyMember(
            leader.getUuid(),
            leader.getName(),
            new Date(),
            leader.getName(),
            null,
            true,
            false,
            false
        ) );
        final PartyCreationJob partyCreationJob = new PartyCreationJob( party );

        BuX.getInstance().getJobManager().executeJob( partyCreationJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().registerParty( party );
        }

        return party;
    }

    @Override
    public Optional<Party> getCurrentPartyFor( final String userName )
    {
        return parties
            .stream()
            .filter( party -> party.getPartyMembers()
                .stream()
                .anyMatch( partyMember -> partyMember.getUserName().equals( userName ) ) )
            .findFirst();
    }

    @Override
    public Optional<Party> getCurrentPartyByUuid( final UUID uuid )
    {
        return parties
            .stream()
            .filter( party -> party.getUuid().equals( uuid ) )
            .findFirst();
    }

    @Override
    public void removeParty( final Party party )
    {
        final PartyRemovalJob partyRemovalJob = new PartyRemovalJob( party );

        BuX.getInstance().getJobManager().executeJob( partyRemovalJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().unregisterParty( party );
        }
    }

    @Override
    public void removeParty( final UUID uuid )
    {
        this.getCurrentPartyByUuid( uuid ).ifPresent( this::removeParty );
    }

    @Override
    public void registerPartyLocally( final Party party )
    {
        this.parties.add( party );
    }

    @Override
    public void unregisterPartyLocally( final Party party )
    {
        this.parties.removeIf( p -> p.getUuid().equals( party.getUuid() ) );
    }

    @Override
    public void addMemberToParty( final Party party, final PartyMember member )
    {
        final PartyAddMemberJob partyAddMemberJob = new PartyAddMemberJob( party, member );

        BuX.getInstance().getJobManager().executeJob( partyAddMemberJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().addMemberToParty( party, member );
        }
    }

    @Override
    public void removeMemberFromParty( final Party party, final PartyMember member )
    {
        if ( party.getPartyMembers().size() <= 1 )
        {
            // the owner was the last party member, so removing party
            removeParty( party );
            return;
        }

        if ( member.isPartyOwner() )
        {
            final Optional<PartyMember> partyMember = party.getPartyMembers()
                .stream()
                .filter( m -> !m.getUuid().equals( member.getUuid() ) )
                .findFirst();

            if ( partyMember.isPresent() )
            {
                this.setPartyOwner( party, partyMember.get(), true );
            }
            else
            {
                // could not assign new party owner, removing party
                removeParty( party );
                return;
            }
        }

        final PartyRemoveMemberJob partyRemoveMemberJob = new PartyRemoveMemberJob( party, member );

        BuX.getInstance().getJobManager().executeJob( partyRemoveMemberJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().removeMemberFromParty( party, member );
        }
    }

    @Override
    public void addInvitationToParty( final Party party, final PartyInvite invite )
    {
        final PartyAddInvitationJob partyAddInvitationJob = new PartyAddInvitationJob( party, invite );

        BuX.getInstance().getJobManager().executeJob( partyAddInvitationJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().addInviteToParty( party, invite );
        }
    }

    @Override
    public void removeInvitationFromParty( final Party party, final PartyInvite invite )
    {
        final PartyRemoveInvitationJob partyRemoveInvitationjob = new PartyRemoveInvitationJob( party, invite );

        BuX.getInstance().getJobManager().executeJob( partyRemoveInvitationjob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().removeInviteFromParty( party, invite );
        }
    }

    @Override
    public void addJoinRequestToParty( final Party party, final PartyJoinRequest joinRequest )
    {
        final PartyAddJoinRequestJob partyAddJoinRequestJob = new PartyAddJoinRequestJob( party, joinRequest );

        BuX.getInstance().getJobManager().executeJob( partyAddJoinRequestJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().addJoinRequestToParty( party, joinRequest );
        }
    }

    @Override
    public void removeJoinRequestFromParty( final Party party, final PartyJoinRequest joinRequest )
    {
        final PartyRemoveJoinRequestJob partyRemoveJoinRequest = new PartyRemoveJoinRequestJob( party, joinRequest );

        BuX.getInstance().getJobManager().executeJob( partyRemoveJoinRequest );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().removeJoinRequestFromParty( party, joinRequest );
        }
    }

    @Override
    public void setPartyOwner( final Party party, final PartyMember member, final boolean owner )
    {
        member.setPartyOwner( owner );

        final PartySetOwnerJob partySetOwnerJob = new PartySetOwnerJob( party, member.getUuid(), owner );

        BuX.getInstance().getJobManager().executeJob( partySetOwnerJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().setOwnerStatus( party, member, owner );
        }
    }

    @Override
    public void broadcastToParty( final Party party, final String message, final HasMessagePlaceholders placeholders )
    {
        for ( PartyMember partyMember : party.getPartyMembers() )
        {
            final UserMessageJob userMessageJob = new UserMessageJob(
                partyMember.getUuid(),
                Utils.replacePlaceHolders( message, placeholders )
            );

            BuX.getInstance().getJobManager().executeJob( userMessageJob );
        }
    }

    @Override
    public void languageBroadcastToParty( final Party party, final String messagePath, final HasMessagePlaceholders placeholders )
    {
        for ( PartyMember partyMember : party.getPartyMembers() )
        {
            final UserLanguageMessageJob userLanguageMessageJob = new UserLanguageMessageJob(
                partyMember.getUuid(),
                messagePath,
                placeholders.getMessagePlaceholders()
            );

            BuX.getInstance().getJobManager().executeJob( userLanguageMessageJob );
        }
    }

    @Override
    public void setChatMode( final Party party, final PartyMember partyMember, final boolean chat )
    {
        partyMember.setChat( chat );

        final PartySetChatModeJob partySetChatModeJob = new PartySetChatModeJob( party, partyMember.getUuid(), chat );

        BuX.getInstance().getJobManager().executeJob( partySetChatModeJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().setChatStatus( party, partyMember, chat );
        }
    }

    @Override
    public void setPartyMemberRole( final Party party, final PartyMember partyMember, final PartyRole partyRole )
    {
        partyMember.setPartyRole( partyRole );

        final String partyRoleName = Optional.ofNullable( partyRole ).map( PartyRole::getName ).orElse( null );
        final PartySetPartyMemberRoleJob partySetPartyMemberRoleJob = new PartySetPartyMemberRoleJob( party, partyMember.getUuid(), partyRoleName );
        BuX.getInstance().getJobManager().executeJob( partySetPartyMemberRoleJob );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            BuX.getInstance().getRedisManager().getDataManager().getRedisPartyDataManager().setPartyMemberRole(
                party,
                partyMember,
                partyRoleName
            );
        }
    }

    private void startPartyCleanupTask()
    {
        final int period = ConfigFiles.PARTY_CONFIG.getPartyInactivityPeriod();

        if ( period > 0 )
        {
            BuX.getInstance().getScheduler().runTaskRepeating( period, period, TimeUnit.SECONDS, () ->
            {
                if ( BuX.getInstance().isRedisManagerEnabled() )
                {
                    final IRedisDataManager redisDataManager = BuX.getInstance().getRedisManager().getDataManager();

                    if ( !redisDataManager.attemptShedLock( "PARTY_CLEANUP", period, TimeUnit.SECONDS ) )
                    {
                        return;
                    }
                }

                final List<Party> queuedForRemoval = new ArrayList<>();

                for ( Party party : parties )
                {
                    final boolean partyInactive = party.getPartyMembers()
                        .stream()
                        .noneMatch( partyMember -> BuX.getApi().getPlayerUtils().isOnline( partyMember.getUserName() ) );

                    if ( party.isInactive() && partyInactive )
                    {
                        queuedForRemoval.add( party );
                    }
                    else
                    {
                        if ( party.isInactive() != partyInactive && BuX.getInstance().isRedisManagerEnabled() )
                        {
                            BuX.getInstance().getRedisManager().getDataManager()
                                .getRedisPartyDataManager().setInactiveStatus( party, partyInactive );
                        }

                        party.setInactive( partyInactive );
                    }
                }

                queuedForRemoval.forEach( this::removeParty );
            } );
        }

        final int memberPeriod = ConfigFiles.PARTY_CONFIG.getPartyInactivityPeriod();

        if ( memberPeriod > 0 )
        {
            BuX.getInstance().getScheduler().runTaskRepeating( memberPeriod, memberPeriod, TimeUnit.SECONDS, () ->
            {
                for ( Party party : this.parties )
                {
                    final List<PartyMember> membersQueuedForRemoval = new ArrayList<>();

                    for ( PartyMember partyMember : party.getPartyMembers() )
                    {
                        final boolean inactive = !BuX.getApi().getPlayerUtils().isOnline( partyMember.getUserName() );

                        if ( partyMember.isInactive() && inactive )
                        {
                            membersQueuedForRemoval.add( partyMember );
                        }
                        else
                        {
                            if ( partyMember.isInactive() != inactive && BuX.getInstance().isRedisManagerEnabled() )
                            {
                                BuX.getInstance().getRedisManager().getDataManager()
                                    .getRedisPartyDataManager().setInactiveStatus( party, partyMember, inactive );
                            }

                            partyMember.setInactive( inactive );
                        }
                    }

                    membersQueuedForRemoval.forEach( member ->
                    {
                        this.removeMemberFromParty( party, member );
                        languageBroadcastToParty( party, "party.inactivity-removal", MessagePlaceholders.create().append( "user", member.getUserName() ) );
                    } );
                }
            } );
        }
    }

    private int getPartyLimit( final User leader )
    {
        return ConfigFiles.PARTY_CONFIG.getConfig().getSectionList( "member-limits" )
            .stream()
            .filter( section -> leader.hasPermission( section.getString( "permission" ) ) )
            .map( section -> section.getInteger( "limit" ) )
            .findFirst()
            .orElse( ConfigFiles.PARTY_CONFIG.getConfig().getInteger( "default-member-limit" ) );
    }
}
