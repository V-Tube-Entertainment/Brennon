package com.gizmo.brennon.core.api.party;

import com.gizmo.brennon.core.api.language.LanguageConfig;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRole;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRolePermission;

import java.util.Optional;
import java.util.UUID;

public class PartyUtils
{

    private PartyUtils()
    {
    }

    public static boolean hasPermission( final Party party, final User user, final PartyRolePermission permission )
    {
        return party.isOwner( user.getUuid() ) || party.getPartyMembers()
            .stream()
            .filter( m -> m.getUuid().equals( user.getUuid() ) )
            .findAny()
            .map( m -> m.getPartyRole() != null && m.getPartyRole().getPermissions().contains( permission ) )
            .orElse( false );
    }

    public static String getRoleName( final Party party, final UUID uuid, final LanguageConfig languageConfig )
    {
        final String noRole = languageConfig.getConfig().getString( "party.list.members.no-role" );

        return party.getPartyMembers()
            .stream()
            .filter( it -> it.getUuid().equals( uuid ) )
            .findFirst()
            .map( it ->
            {
                if ( it.getUuid().equals( party.getOwner().getUuid() ) )
                {
                    return languageConfig.getConfig().getString( "party.owner-role-name" );
                }
                else
                {
                    return Optional.ofNullable( it.getPartyRole() )
                        .map( PartyRole::getName )
                        .orElse( noRole );
                }
            } )
            .orElse( noRole );
    }
}
