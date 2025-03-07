package com.gizmo.brennon.core.protocolize.guis.friends.friendactions;

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
import com.gizmo.brennon.core.protocolize.guis.friends.friend.FriendGuiConfigItem;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.data.ItemType;

import java.util.Optional;

public class FriendActionsItemPage extends ItemPage
{

    public FriendActionsItemPage( final User user, final FriendActionsGuiConfig guiConfig, final FriendData friendData )
    {
        super( guiConfig.getRows() * 9 );
        final String currentServer = Optional.ofNullable( BuX.getApi().getPlayerUtils().findPlayer( friendData.getFriend() ) )
            .map( IProxyServer::getName ).orElse( null );

        final MessagePlaceholders placeholders = MessagePlaceholders.create()
            .append( "friend-name", friendData.getFriend() )
            .append( "last-online", Utils.formatDate( friendData.getLastOnline() ) )
            .append( "server", currentServer == null ? "Unknown" : currentServer );

        for ( GuiConfigItem item : guiConfig.getItems() )
        {
            for ( int slot : item.getSlots() )
            {
                if ( ( (FriendGuiConfigItem) item ).isFriendItem() )
                {
                    super.setItem( slot, this.getFriendGuiItem(
                        user,
                        (FriendGuiConfigItem) item,
                        friendData,
                        currentServer,
                        placeholders
                    ) );
                }
                else
                {
                    super.setItem( slot, this.getGuiItem(
                        user,
                        item,
                        placeholders
                    ) );
                }
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
