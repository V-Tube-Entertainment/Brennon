package com.gizmo.brennon.core.api.redis;

import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyInvite;
import com.gizmo.brennon.core.api.party.PartyJoinRequest;
import com.gizmo.brennon.core.api.party.PartyMember;

import java.util.List;

public interface PartyDataManager
{

    void registerParty( Party party );

    void unregisterParty( Party party );

    void addMemberToParty( Party party, PartyMember partyMember );

    void removeMemberFromParty( Party party, PartyMember partyMember );

    void setInactiveStatus( Party party, boolean inactive );

    void setInactiveStatus( Party party, PartyMember partyMember, boolean inactive );

    void setOwnerStatus( Party party, PartyMember partyMember, boolean owner );

    void setChatStatus( Party party, PartyMember partyMember, boolean chat );

    void setPartyMemberRole( Party party, PartyMember partyMember, String partyRole );

    void addInviteToParty( Party party, PartyInvite partyInvite );

    void removeInviteFromParty( Party party, PartyInvite partyInvite );

    void addJoinRequestToParty( Party party, PartyJoinRequest partyJoinRequest );

    void removeJoinRequestFromParty( Party party, PartyJoinRequest partyJoinRequest );

    List<Party> getAllParties();

}
