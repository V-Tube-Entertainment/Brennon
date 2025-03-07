package com.gizmo.brennon.core.protocolize.guis.parties;

import com.gizmo.brennon.core.api.party.Party;
import com.gizmo.brennon.core.api.party.PartyMember;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.protocolize.gui.ItemPage;
import com.gizmo.brennon.core.protocolize.gui.PageableItemProvider;
import com.gizmo.brennon.core.protocolize.gui.item.GuiItem;

import java.util.List;
import java.util.Optional;

public class PartyGuiItemProvider implements PageableItemProvider
{

    private final ItemPage[] pages;

    public PartyGuiItemProvider( final User user,
                                 final PartyGuiConfig config,
                                 final Party party,
                                 final List<PartyMember> members )
    {
        final int itemsPerPage = config.getItems().stream()
            .filter( item -> ( (PartyGuiConfigItem) item ).isMemberItem() )
            .mapToInt( item -> item.getSlots().size() )
            .sum();
        int pages = (int) Math.ceil( (double) members.size() / (double) itemsPerPage );
        if ( pages == 0 )
        {
            pages = 1;
        }
        this.pages = new ItemPage[pages];

        for ( int i = 0; i < pages; i++ )
        {
            final int max = ( i + 1 ) * itemsPerPage;

            this.pages[i] = new PartyItemPage(
                user,
                i,
                pages,
                config,
                party,
                members.isEmpty() ? members : members.size() <= max ? members : members.subList( i * itemsPerPage, max )
            );
        }
    }

    @Override
    public Optional<GuiItem> getItemAtSlot( final int page, final int rawSlot )
    {
        return this.getItemContents( page ).getItem( rawSlot );
    }

    @Override
    public ItemPage getItemContents( int page )
    {
        if ( page == 0 )
        {
            page = 1;
        }
        if ( page > pages.length )
        {
            page = pages.length;
        }
        return pages[page - 1];
    }

    @Override
    public int getPageAmount()
    {
        return pages.length;
    }
}
