package com.gizmo.brennon.core.protocolize.guis.parties;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.party.PartyUtils;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.configs.PartyConfig.PartyRolePermission;
import com.gizmo.brennon.core.api.utils.placeholders.HasMessagePlaceholders;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.config.GuiConfigItem;
import com.gizmo.brennon.core.protocolize.gui.item.GuiItem;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class PartyItemPage extends ItemPage
{

    public PartyItemPage( final User user,
                          final int page,
                          final int max,
                          final PartyGuiConfig guiConfig,
                          final Party party,
                          final List<PartyMember> partyMembers )
    {
        super( guiConfig.getRows() * 9 );

        for ( GuiConfigItem item : guiConfig.getItems() )
        {
            if ( ( (PartyGuiConfigItem) item ).isMemberItem() )
            {
                continue;
            }
            if ( !this.shouldShow( user, page, max, item ) )
            {
                continue;
            }
            for ( int slot : item.getSlots() )
            {
                super.setItem( slot, this.getGuiItem( user, item ) );
            }
        }

        for ( GuiConfigItem item : guiConfig.getItems() )
        {
            if ( !( (PartyGuiConfigItem) item ).isMemberItem() )
            {
                continue;
            }

            final Iterator<PartyMember> partyMemberIterator = partyMembers.iterator();
            for ( int slot : item.getSlots() )
            {
                if ( !partyMemberIterator.hasNext() )
                {
                    break;
                }
                final PartyMember member = partyMemberIterator.next();
                final String currentServer = Optional.ofNullable( BuX.getApi().getPlayerUtils().findPlayer( member.getUserName() ) )
                    .map( IProxyServer::getName ).orElse( null );

                super.setItem( slot, this.getPartyMemberGuiItem(
                    user,
                    (PartyGuiConfigItem) item,
                    member,
                    currentServer,
                    MessagePlaceholders.create()
                        .append( "member", member.getUserName() )
                        .append( "server", currentServer == null ? "Unknown" : currentServer )
                        .append( "role", PartyUtils.getRoleName( party, member.getUuid(), user.getLanguageConfig() ) )
                ) );
            }
        }
    }

    @Override
    protected boolean shouldShow( final User user, final int page, final int max, final GuiConfigItem item )
    {
        if ( item.getShowIf().startsWith( "has-party-permission:" ) )
        {
            final PartyRolePermission partyRolePermission = Utils.valueOfOr(
                PartyRolePermission.class,
                item.getShowIf().replaceFirst( "has-party-permission:", "" ),
                null
            );
            final Party party = BuX.getInstance().getPartyManager().getCurrentPartyFor( user.getName() ).orElse( null );

            if ( partyRolePermission != null && party != null )
            {
                return PartyUtils.hasPermission( party, user, partyRolePermission );
            }
        }
        else if ( item.getShowIf().equalsIgnoreCase( "is-party-owner" ) )
        {
            final Party party = BuX.getInstance().getPartyManager().getCurrentPartyFor( user.getName() ).orElse( null );

            return party != null && party.isOwner( user.getUuid() );
        }

        return super.shouldShow( user, page, max, item );
    }

    private GuiItem getPartyMemberGuiItem( final User user,
                                           final PartyGuiConfigItem item,
                                           final PartyMember member,
                                           final String currentServer,
                                           final HasMessagePlaceholders placeholders )
    {
        final boolean online = currentServer != null;
        final ItemStack itemStack = online
            ? item.getOnlineItem().buildItem( user, placeholders )
            : item.getOfflineItem().buildItem( user, placeholders );

        if ( itemStack.itemType() == ItemType.PLAYER_HEAD )
        {
            itemStack.nbtData().putString( "SkullOwner", member.getUserName() );
        }

        return this.getGuiItem( item.getAction(), item.getRightAction(), itemStack, placeholders );
    }
}
