package com.gizmo.brennon.core.placeholders;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.placeholder.PlaceHolderAPI;
import com.gizmo.brennon.core.api.placeholder.PlaceHolderPack;
import com.gizmo.brennon.core.api.placeholder.event.PlaceHolderEvent;

public class PermissionPlaceHolderPack implements PlaceHolderPack
{

    @Override
    public void loadPack()
    {
        PlaceHolderAPI.addPlaceHolder( "{permission_user_prefix}", true, this::getPermissionUserPrefix );
        PlaceHolderAPI.addPlaceHolder( "{permission_user_suffix}", true, this::getPermissionUserSuffix );
        PlaceHolderAPI.addPlaceHolder( "{permission_user_primary_group}", true, this::getPermissionUserPrimaryGroup );
    }

    private String getPermissionUserPrefix( final PlaceHolderEvent event )
    {
        return BuX.getInstance().getActivePermissionIntegration().getPrefix( event.getUser().getUuid() );
    }

    private String getPermissionUserSuffix( final PlaceHolderEvent event )
    {
        return BuX.getInstance().getActivePermissionIntegration().getSuffix( event.getUser().getUuid() );
    }

    private String getPermissionUserPrimaryGroup( final PlaceHolderEvent event )
    {
        return BuX.getInstance().getActivePermissionIntegration().getGroup( event.getUser().getUuid() ).join();
    }
}
