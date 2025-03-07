package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.event.Priority;
import com.gizmo.brennon.core.api.event.events.user.UserChatEvent;
import com.gizmo.brennon.core.api.job.jobs.ChatSyncJob;
import com.gizmo.brennon.core.api.placeholder.PlaceHolderAPI;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.config.configs.ChatSyncConfig.ChatSyncedServer;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.Optional;

public class ChatSyncExecutor implements EventExecutor
{

    @Event( priority = Priority.HIGHEST )
    public void onChat( final UserChatEvent event )
    {
        if ( event.isCancelled() || !ConfigFiles.CHAT_SYNC_CONFIG.isEnabled() )
        {
            return;
        }
        final User user = event.getUser();
        final Optional<ChatSyncedServer> optionalChatSyncedServer = ConfigFiles.CHAT_SYNC_CONFIG.getChatSyncedServer(
            user.getServerName()
        );

        optionalChatSyncedServer.ifPresent( chatSyncedServer ->
        {
            if ( chatSyncedServer.forceFormat() )
            {
                event.setCancelled( true );
            }

            final String message = Utils.replacePlaceHolders(
                chatSyncedServer.format(),
                str -> PlaceHolderAPI.formatMessage( user, str ),
                null,
                MessagePlaceholders.create()
                    .append( "message", event.getMessage() )
            );

            BuX.getInstance().getJobManager().executeJob( new ChatSyncJob(
                chatSyncedServer.serverGroup().getName(),
                chatSyncedServer.forceFormat() ? null : user.getServerName(),
                message
            ) );
        } );
    }
}
