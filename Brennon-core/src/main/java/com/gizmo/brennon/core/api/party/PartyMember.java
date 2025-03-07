package com.gizmo.brennon.core.api.party;

import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class PartyMember
{

    private final UUID uuid;
    private final String userName;
    private final Date joinedAt;
    private String nickName;
    private PartyRole partyRole;
    private boolean partyOwner;
    private boolean inactive;
    private boolean chat;

    public static PartyMember fromMap( final UUID partyMemberUuid, final Map<String, String> memberData )
    {
        return new PartyMember(
            partyMemberUuid,
            memberData.get( "userName" ),
            new Date( Long.parseLong( memberData.get( "joinedAt" ) ) ),
            memberData.get( "nickName" ),
            ConfigFiles.PARTY_CONFIG.findPartyRole( memberData.getOrDefault( "partyRole", null ) )
                .orElse( ConfigFiles.PARTY_CONFIG.getDefaultRole() ),
            Boolean.parseBoolean( memberData.get( "partyOwner" ) ),
            Boolean.parseBoolean( memberData.get( "inactive" ) ),
            Boolean.parseBoolean( memberData.get( "chat" ) )
        );
    }

    public Map<String, String> asMap()
    {
        final Map<String, String> memberData = new HashMap<>();

        memberData.put( "userName", userName );
        memberData.put( "joinedAt", String.valueOf( joinedAt.getTime() ) );
        memberData.put( "nickName", nickName );
        memberData.put( "partyRole", Optional.ofNullable( partyRole ).map( PartyRole::getName ).orElse( "" ) );
        memberData.put( "partyOwner", String.valueOf( partyOwner ) );
        memberData.put( "inactive", String.valueOf( inactive ) );
        memberData.put( "chat", String.valueOf( chat ) );

        return memberData;
    }

    public int getPartyRolePriority()
    {
        return Optional.ofNullable( partyRole ).map( PartyRole::getPriority ).orElse( 0 );
    }
}
