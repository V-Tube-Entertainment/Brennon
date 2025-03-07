package com.gizmo.brennon.core.protocolize.guis.friends.friend;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.friends.FriendData;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
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

public class FriendItemPage extends ItemPage
{

    public FriendItemPage( final User user,
                           final int page,
                           final int max,
                           final FriendGuiConfig guiConfig,
                           final List<FriendData> friendData )
    {
        super( guiConfig.getRows() * 9 );

        for ( GuiConfigItem item : guiConfig.getItems() )
        {
            if ( ( (FriendGuiConfigItem) item ).isFriendItem() )
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
            if ( !( (FriendGuiConfigItem) item ).isFriendItem() )
            {
                continue;
            }

            final Iterator<FriendData> friendDataIterator = friendData.iterator();
            for ( int slot : item.getSlots() )
            {
                if ( !friendDataIterator.hasNext() )
                {
                    break;
                }
                final FriendData data = friendDataIterator.next();
                final String currentServer = Optional.ofNullable( BuX.getApi().getPlayerUtils().findPlayer( data.getFriend() ) )
                    .map( IProxyServer::getName ).orElse( null );

                super.setItem( slot, this.getFriendGuiItem(
                    user,
                    (FriendGuiConfigItem) item,
                    data,
                    currentServer,
                    MessagePlaceholders.create()
                        .append( "friend-name", data.getFriend() )
                        .append( "last-online", Utils.formatDate( data.getLastOnline() ) )
                        .append( "server", currentServer == null ? "Unknown" : currentServer )
                ) );
            }
        }
    }

    private GuiItem getFriendGuiItem( final User user,
                                      final FriendGuiConfigItem item,
                                      final FriendData friendData,
                                      final String currentServer,
                                      final HasMessagePlaceholders placeholders )
    {
        final boolean online = currentServer != null;
        final ItemStack itemStack = online
            ? item.getOnlineItem().buildItem( user, placeholders )
            : item.getOfflineItem().buildItem( user, placeholders );

        if ( itemStack.itemType() == ItemType.PLAYER_HEAD )
        {
            itemStack.nbtData().putString( "SkullOwner", friendData.getFriend() );
        }

        return this.getGuiItem( item.getAction(), item.getRightAction(), itemStack, placeholders );
    }
}
