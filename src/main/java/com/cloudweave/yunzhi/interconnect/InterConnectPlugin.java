/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cloudweave.yunzhi.interconnect;

import com.cloudweave.yunzhi.interconnect.api.InterConnectAPI;
import com.cloudweave.yunzhi.interconnect.commands.InterConnectCommand;
import com.cloudweave.yunzhi.interconnect.config.ConfigManager;
import com.cloudweave.yunzhi.interconnect.config.MessageManager;
import com.cloudweave.yunzhi.interconnect.listeners.PlayerEventListener;
import com.cloudweave.yunzhi.interconnect.metrics.Metrics;
import com.cloudweave.yunzhi.interconnect.update.UpdateChecker;
import com.cloudweave.yunzhi.interconnect.websocket.WebSocketManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * InterConnect Spigot Plugin Main Class
 * 
 * A Minecraft Spigot plugin that connects to InterConnect-Server via WebSocket
 * to enable cross-server player event synchronization.
 * 
 * @author CloudWeave-YunZhi
 * @version 1.1.0
 */
public class InterConnectPlugin extends JavaPlugin {

    private static InterConnectPlugin instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private WebSocketManager webSocketManager;
    private UpdateChecker updateChecker;
    private Logger logger;

    // bStats Metrics ID
    private static final int BSTATS_PLUGIN_ID = 22543;

    // ASCII Art Logo
    private static final String[] LOGO_LINES = {
        "  _____ _____ _____      _____       _             _   ",
        " |_   _/ ____/ ____|    / ____|     (_)           | |  ",
        "   | || |   | |   _____| (___  _ __  _  __ _  ___ | |_ ",
        "   | || |   | |  |______\\___ \\| '_ \\| |/ _` |/ _ \\| __|",
        "  _| || |___| |____     ____) | |_) | | (_| | (_) | |_ ",
        " |_____\\_____\\_____|   |_____/| .__/|_|\\__, |\\___/ \\__|",
        "                              | |       __/ |          ",
        "                              |_|      |___/           "
    };

    @Override
    public void onEnable() {
        instance = this;
        this.logger = getLogger();

        // Print ASCII logo
        printLogo();

        // Initialize configuration
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        // Initialize message manager
        this.messageManager = new MessageManager(this);
        this.messageManager.loadMessages();

        // Initialize WebSocket manager
        this.webSocketManager = new WebSocketManager(this);

        // Initialize API
        InterConnectAPI.initialize(this);

        // Register commands
        InterConnectCommand command = new InterConnectCommand(this);
        getCommand("ic").setExecutor(command);
        getCommand("ic").setTabCompleter(command);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        // Initialize bStats
        initMetrics();

        // Initialize update checker
        if (configManager.getConfig().getBoolean("check-for-updates", true)) {
            this.updateChecker = new UpdateChecker(this);
            updateChecker.checkForUpdates();
        }

        // Connect to WebSocket server if auto-connect is enabled
        if (configManager.isAutoConnect()) {
            getServer().getScheduler().runTaskLater(this, () -> {
                webSocketManager.connect();
            }, 20L); // Delay 1 second to let server fully start
        }

        logger.info("InterConnect Client v" + getDescription().getVersion() + " has been enabled!");
        logger.info("GitHub: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot");
    }

    @Override
    public void onDisable() {
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
        
        logger.info("InterConnect Client has been disabled!");
    }

    /**
     * Initialize bStats metrics
     */
    private void initMetrics() {
        try {
            Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            
            // Add custom charts
            metrics.addCustomChart(new Metrics.SimplePie("language", () -> 
                getConfig().getString("language", "zh_CN")));
            
            metrics.addCustomChart(new Metrics.SimplePie("websocket_connected", () -> 
                webSocketManager.isConnected() ? "Yes" : "No"));
            
            getConfigManager().debug("bStats metrics enabled");
        } catch (Exception e) {
            getConfigManager().debug("Failed to initialize bStats: " + e.getMessage());
        }
    }

    /**
     * Print the ASCII art logo to console
     */
    private void printLogo() {
        for (String line : LOGO_LINES) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }

    /**
     * Get the plugin instance
     * 
     * @return InterConnectPlugin instance
     */
    public static InterConnectPlugin getInstance() {
        return instance;
    }

    /**
     * Get the configuration manager
     * 
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the message manager
     * 
     * @return MessageManager instance
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Get the WebSocket manager
     * 
     * @return WebSocketManager instance
     */
    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    /**
     * Get the update checker
     * 
     * @return UpdateChecker instance
     */
    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    /**
     * Reload plugin configuration
     */
    public void reload() {
        // Reload main config
        reloadConfig();
        configManager.loadConfig();
        
        // Reload messages
        messageManager.reload();
        
        // Reconnect if currently connected or if auto-connect is enabled
        if (webSocketManager.isConnected()) {
            webSocketManager.disconnect();
            webSocketManager.connect();
        } else if (configManager.isAutoConnect()) {
            webSocketManager.connect();
        }
        
        logger.info("Configuration reloaded!");
    }
}
