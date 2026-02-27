/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.config;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration Manager for InterConnect Plugin
 * Handles all configuration loading and access
 */
public class ConfigManager {

    private final InterConnectPlugin plugin;
    private FileConfiguration config;

    // Configuration fields with defaults
    private String serverUrl;
    private String serverUuid;
    private String serverToken;
    private String serverName;
    private boolean autoConnect;
    private boolean syncPlayerJoin;
    private boolean syncPlayerQuit;
    private boolean syncPlayerDeath;
    private boolean syncPlayerChat;
    private boolean syncPlayerMessage;
    private int reconnectInterval;
    private int reconnectMaxAttempts;
    private int heartbeatInterval;
    private boolean debugMode;

    public ConfigManager(InterConnectPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Load configuration from config.yml
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Server connection settings
        this.serverUrl = config.getString("server.url", "ws://localhost:8000/ws");
        this.serverUuid = config.getString("server.uuid", "");
        this.serverToken = config.getString("server.token", "");
        this.serverName = config.getString("server.name", "MyServer");
        this.autoConnect = config.getBoolean("server.auto-connect", true);

        // Event sync settings
        this.syncPlayerJoin = config.getBoolean("events.sync-player-join", true);
        this.syncPlayerQuit = config.getBoolean("events.sync-player-quit", true);
        this.syncPlayerDeath = config.getBoolean("events.sync-player-death", true);
        this.syncPlayerChat = config.getBoolean("events.sync-player-chat", true);
        this.syncPlayerMessage = config.getBoolean("events.sync-player-message", true);

        // Connection settings
        this.reconnectInterval = config.getInt("connection.reconnect-interval", 5);
        this.reconnectMaxAttempts = config.getInt("connection.reconnect-max-attempts", 10);
        this.heartbeatInterval = config.getInt("connection.heartbeat-interval", 30);
        this.debugMode = config.getBoolean("connection.debug-mode", false);
    }

    // Getters

    public String getServerUrl() {
        return serverUrl;
    }

    public String getServerUuid() {
        return serverUuid;
    }

    public String getServerToken() {
        return serverToken;
    }

    public String getServerName() {
        return serverName;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public boolean isSyncPlayerJoin() {
        return syncPlayerJoin;
    }

    public boolean isSyncPlayerQuit() {
        return syncPlayerQuit;
    }

    public boolean isSyncPlayerDeath() {
        return syncPlayerDeath;
    }

    public boolean isSyncPlayerChat() {
        return syncPlayerChat;
    }

    public boolean isSyncPlayerMessage() {
        return syncPlayerMessage;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public int getReconnectMaxAttempts() {
        return reconnectMaxAttempts;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isValid() {
        if (serverUuid == null || serverUuid.isEmpty()) {
            return false;
        }
        if (serverToken == null || serverToken.isEmpty()) {
            return false;
        }
        if (serverUrl == null || serverUrl.isEmpty()) {
            return false;
        }
        return true;
    }

    public String getValidationError() {
        if (serverUuid == null || serverUuid.isEmpty()) {
            return "Server UUID is not configured";
        }
        if (serverToken == null || serverToken.isEmpty()) {
            return "Server Token is not configured";
        }
        if (serverUrl == null || serverUrl.isEmpty()) {
            return "Server URL is not configured";
        }
        return null;
    }

    /**
     * Log debug message if debug mode is enabled
     * 
     * @param message The message to log
     */
    public void debug(String message) {
        if (debugMode) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
}
