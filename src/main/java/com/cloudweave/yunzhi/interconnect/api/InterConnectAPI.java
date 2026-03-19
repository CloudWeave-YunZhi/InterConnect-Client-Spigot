/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.api;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import com.cloudweave.yunzhi.interconnect.websocket.WebSocketManager;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * InterConnect API
 * 
 * Provides a public interface for other plugins to interact with InterConnect.
 * 
 * <p>Usage example:</p>
 * <pre>
 * InterConnectAPI api = InterConnectAPI.getInstance();
 * if (api != null && api.isConnected()) {
 *     api.broadcastMessage("player_message", "Hello from my plugin!");
 * }
 * </pre>
 * 
 * @author CloudWeave-YunZhi
 * @version 1.1.0
 */
public class InterConnectAPI {

    private static InterConnectAPI instance;
    private final InterConnectPlugin plugin;

    private InterConnectAPI(InterConnectPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the API instance. Called internally by the plugin.
     * 
     * @param plugin The InterConnectPlugin instance
     */
    public static void initialize(InterConnectPlugin plugin) {
        if (instance == null) {
            instance = new InterConnectAPI(plugin);
        }
    }

    /**
     * Get the API instance.
     * 
     * @return The InterConnectAPI instance, or null if the plugin is not loaded
     */
    @Nullable
    public static InterConnectAPI getInstance() {
        return instance;
    }

    /**
     * Check if connected to the InterConnect-Server.
     * 
     * @return true if connected
     */
    public boolean isConnected() {
        return plugin.getWebSocketManager().isConnected();
    }

    /**
     * Get the configured server name.
     * 
     * @return The server name
     */
    @Nonnull
    public String getServerName() {
        return plugin.getConfigManager().getServerName();
    }

    /**
     * Broadcast a message to all connected servers.
     * 
     * @param eventType One of the event types supported by InterConnect-Server
     * @param message The message to broadcast
     * @return true if the message was sent successfully
     */
    public boolean broadcastMessage(@Nonnull String eventType, @Nonnull String message) {
        if (!isConnected()) {
            return false;
        }

        if (!WebSocketManager.EVENT_PLAYER_MESSAGE.equals(eventType)
            && !WebSocketManager.EVENT_PLAYER_CHAT.equals(eventType)
            && !WebSocketManager.EVENT_QQ_MESSAGE.equals(eventType)) {
            plugin.getLogger().warning("broadcastMessage only supports player_message, player_chat or qq_message");
            return false;
        }

        JSONObject data = new JSONObject();
        data.put("playerName", getServerName());
        data.put("text", message);
        data.put("serverName", getServerName());

        return plugin.getWebSocketManager().broadcast(eventType, data);
    }

    /**
     * Broadcast a JSON message to all connected servers.
     * 
     * @param eventType One of the event types supported by InterConnect-Server
     * @param data The JSON data to broadcast
     * @return true if the message was sent successfully
     */
    public boolean broadcastJson(@Nonnull String eventType, @Nonnull JSONObject data) {
        if (!isConnected()) {
            return false;
        }

        // Add server name to data if not present
        if (!data.has("serverName")) {
            data.put("serverName", getServerName());
        }

        return plugin.getWebSocketManager().broadcast(eventType, data);
    }

    /**
     * Send a message to a specific server.
     * 
     * @param targetUuid The target server's UUID
     * @param eventType One of the event types supported by InterConnect-Server
     * @param data The JSON data to send
     * @return true if the message was sent successfully
     */
    public boolean sendToServer(@Nonnull String targetUuid, @Nonnull String eventType, @Nonnull JSONObject data) {
        if (!isConnected()) {
            return false;
        }

        if (!data.has("serverName")) {
            data.put("serverName", getServerName());
        }

        return plugin.getWebSocketManager().sendMessage(eventType, targetUuid, data);
    }

    /**
     * Broadcast a player-related event to all connected servers.
     * 
     * @param eventType The event type (player_join, player_quit, player_death, player_chat, player_message, qq_message)
     * @param player The player involved in the event
     * @param additionalData Additional data to include in the broadcast
     * @return true if the message was sent successfully
     */
    public boolean broadcastPlayerEvent(@Nonnull String eventType, @Nonnull Player player, @Nullable JSONObject additionalData) {
        if (!isConnected()) {
            return false;
        }

        JSONObject data = new JSONObject();
        data.put("playerName", player.getName());
        data.put("uuid", player.getUniqueId().toString());
        data.put("displayName", player.getDisplayName());
        data.put("serverName", getServerName());

        if (additionalData != null) {
            // Merge additional data
            for (String key : additionalData.keySet()) {
                data.put(key, additionalData.get(key));
            }
        }

        return plugin.getWebSocketManager().broadcast(eventType, data);
    }

    /**
     * Manually connect to the InterConnect-Server.
     * 
     * @return true if connection was initiated
     */
    public boolean connect() {
        if (isConnected()) {
            return false;
        }
        plugin.getWebSocketManager().connect();
        return true;
    }

    /**
     * Disconnect from the InterConnect-Server.
     */
    public void disconnect() {
        plugin.getWebSocketManager().disconnect();
    }

    /**
     * Register a message listener for forwarded server events.
     * 
     * @param listener The listener to register
     */
    public void registerMessageListener(@Nonnull MessageListener listener) {
        plugin.getWebSocketManager().registerMessageListener(listener);
    }

    /**
     * Unregister a message listener.
     * 
     * @param listener The listener to unregister
     */
    public void unregisterMessageListener(@Nonnull MessageListener listener) {
        plugin.getWebSocketManager().unregisterMessageListener(listener);
    }

    /**
     * Get the plugin version.
     * 
     * @return The version string
     */
    @Nonnull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Check if debug mode is enabled.
     * 
     * @return true if debug mode is enabled
     */
    public boolean isDebugMode() {
        return plugin.getConfigManager().isDebugMode();
    }

    /**
     * Log a debug message if debug mode is enabled.
     * 
     * @param message The message to log
     */
    public void debug(@Nonnull String message) {
        plugin.getConfigManager().debug(message);
    }

    /**
     * Check whether an event type is supported by the current InterConnect-Server protocol.
     *
     * @param eventType The event type to validate
     * @return true if the server protocol supports this event type
     */
    public boolean isSupportedEventType(@Nonnull String eventType) {
        return WebSocketManager.isSupportedEventType(eventType);
    }
}
