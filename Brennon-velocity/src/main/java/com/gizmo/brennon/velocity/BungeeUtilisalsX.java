package com.gizmo.brennon.velocity;

import com.gizmo.brennon.velocity.listeners.*;
import com.gizmo.brennon.core.*;
import com.gizmo.brennon.core.api.announcer.AnnouncementType;
import com.gizmo.brennon.core.api.announcer.Announcer;
import com.gizmo.brennon.core.api.utils.Platform;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.other.StaffUser;
import com.gizmo.brennon.core.commands.CommandManager;
import com.gizmo.brennon.core.event.EventLoader;
import com.gizmo.brennon.core.language.PluginLanguageManager;
import com.gizmo.brennon.core.punishment.PunishmentHelper;
import com.gizmo.brennon.core.serverbalancer.SimpleServerBalancer;
import com.gizmo.brennon.velocity.utils.player.RedisPlayerUtils;
import com.gizmo.brennon.velocity.utils.player.VelocityPlayerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BungeeUtilisalsX extends AbstractBungeeUtilisalsX {

    private final ServerOperationsApi serverOperationsApi = new VelocityOperationsApi();
    private final CommandManager commandManager = new CommandManager();
    private final IPluginDescription pluginDescription = new VelocityPluginDescription();
    private final List<StaffUser> staffMembers = new ArrayList<>();

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    protected IBuXApi createBuXApi() {
        SimpleServerBalancer simpleServerBalancer = null;

        if (ConfigFiles.SERVER_BALANCER_CONFIG.isEnabled()) {
            simpleServerBalancer = new SimpleServerBalancer();
            simpleServerBalancer.setup();
        }

        return new BuXApi(
                new PluginLanguageManager(),
                new EventLoader(),
                new PunishmentHelper(),
                ConfigFiles.CONFIG.getConfig().getBoolean("multi-proxy.enabled")
                        ? new RedisPlayerUtils()
                        : new VelocityPlayerUtils(),
                simpleServerBalancer
        );
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    protected void registerListeners() {
        Bootstrap.getInstance().getProxyServer().getEventManager().register(
                Bootstrap.getInstance(), new UserChatListener()
        );
        Bootstrap.getInstance().getProxyServer().getEventManager().register(
                Bootstrap.getInstance(), new UserConnectionListener()
        );
        Bootstrap.getInstance().getProxyServer().getEventManager().register(
                Bootstrap.getInstance(), new PluginMessageListener()
        );

        if (ConfigFiles.PUNISHMENT_CONFIG.isEnabled()) {
            Bootstrap.getInstance().getProxyServer().getEventManager().register(
                    Bootstrap.getInstance(), new PunishmentListener()
            );
        }

        if (ConfigFiles.MOTD.isEnabled()) {
            Bootstrap.getInstance().getProxyServer().getEventManager().register(
                    Bootstrap.getInstance(), new MotdPingListener()
            );
        }
    }

    @Override
    protected void registerPluginSupports() {
        super.registerPluginSupports();
    }

    @Override
    public ServerOperationsApi serverOperations() {
        return serverOperationsApi;
    }

    @Override
    public File getDataFolder() {
        return Bootstrap.getInstance().getDataFolder();
    }

    @Override
    public String getVersion() {
        return pluginDescription.getVersion();
    }

    @Override
    public List<StaffUser> getStaffMembers() {
        return staffMembers;
    }

    @Override
    public IPluginDescription getDescription() {
        return pluginDescription;
    }

    @Override
    public Logger getLogger() {
        return Bootstrap.getInstance().getLogger();
    }

    @Override
    public Platform getPlatform() {
        return Platform.VELOCITYPOWERED;
    }

}
