package com.gizmo.brennon.core.commands.domains;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.UserUtils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;

public class DomainsSearchSubCommandCall implements CommandCall
{

    @Override
    public void onExecute( User user, List<String> args, List<String> parameters )
    {
        if ( args.size() == 0 )
        {
            user.sendLangMessage( "general-commands.domains.search.usage" );
            return;
        }
        final String domainToSearch = args.get( 0 );

        BuX.getApi().getStorageManager().getDao().getUserDao().searchJoinedHosts( domainToSearch ).thenAccept( ( domains ) ->
        {
            user.sendLangMessage( "general-commands.domains.search.header", MessagePlaceholders.create().append( "total", domains.size() ) );

            domains.entrySet().stream()
                .sorted( ( o1, o2 ) -> Integer.compare( o2.getValue(), o1.getValue() ) )
                .forEach( entry ->
                    user.sendLangMessage(
                        "general-commands.domains.search.format",
                        MessagePlaceholders.create()
                            .append( "{domain}", entry.getKey() )
                            .append( "{online}", UserUtils.getOnlinePlayersOnDomain( entry.getKey() ) )
                            .append( "{total}", entry.getValue() )
                    )
                );

            user.sendLangMessage( "general-commands.domains.search.footer", MessagePlaceholders.create().append( "total", domains.size() ) );
        } );
    }

    @Override
    public String getDescription()
    {
        return "Searches domain details for one specific domain, this works similar to /domains list, but instead of all domains it only shows domains that match your input.";
    }

    @Override
    public String getUsage()
    {
        return "/domains search (input)";
    }
}
