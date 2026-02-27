/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.websocket;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import com.cloudweave.yunzhi.interconnect.api.MessageListener;
import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WebSocket Manager for InterConnect Plugin
 * Handles connection to InterConnect-Server and message routing
 */
public class WebSocketManager {

    private final InterConnectPlugin plugin;
    private WebSocketClient webSocketClient;
    private volatile boolean connected = false;
    private int reconnectAttempts = 0;
    private volatile boolean isAlive = true;
    private int heartbeatTaskId = -1;

    // Message listeners for custom events
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<>();

    // Supported event types
    public static final String EVENT_PLAYER_JOIN = "player_join";
    public static final String EVENT_PLAYER_QUIT = "player_quit";
    public static final String EVENT_PLAYER_DEATH = "player_death";
    public static final String EVENT_PLAYER_CHAT = "player_chat";
    public static final String EVENT_PLAYER_MESSAGE = "player_message";

    // Built-in event types set
    private static final Set<String> BUILTIN_EVENTS = new HashSet<>(Arrays.asList(
        EVENT_PLAYER_JOIN, EVENT_PLAYER_QUIT, EVENT_PLAYER_DEATH, 
        EVENT_PLAYER_CHAT, EVENT_PLAYER_MESSAGE
    ));

    public WebSocketManager(InterConnectPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Connect to the InterConnect-Server WebSocket
     */
    public void connect() {
        if (connected || webSocketClient != null) {
            plugin.getLogger().warning("Already connected or connecting!");
            return;
        }

        if (!plugin.getConfigManager().isValid()) {
            plugin.getLogger().warning("Cannot connect: Server UUID or Token is not configured!");
            plugin.getLogger().warning("Please configure 'server.uuid' and 'server.token' in config.yml");
            return;
        }

        String serverUrl = plugin.getConfigManager().getServerUrl();
        String uuid = plugin.getConfigManager().getServerUuid();
        String token = plugin.getConfigManager().getServerToken();

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-uuid", uuid);
            headers.put("x-token", token);

            webSocketClient = new WebSocketClient(new URI(serverUrl), headers) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        connected = true;
                        reconnectAttempts = 0;
                        plugin.getLogger().info("Successfully connected to InterConnect-Server!");
                        plugin.getLogger().info("Server: " + serverUrl);
                        startHeartbeat();
                    });
                }

                @Override
                public void onMessage(String message) {
                    isAlive = true;
                    Bukkit.getScheduler().runTask(plugin, () -> handleIncomingMessage(message));
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        connected = false;
                        stopHeartbeat();
                        if (remote) {
                            plugin.getLogger().warning("Connection closed by server. Code: " + code + ", Reason: " + reason);
                        } else {
                            plugin.getLogger().info("Connection closed. Code: " + code);
                        }
                        
                        // Attempt reconnection if enabled
                        if (plugin.getConfigManager().isAutoConnect()) {
                            scheduleReconnect();
                        }
                    });
                }

                @Override
                public void onError(Exception ex) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().warning("WebSocket error: " + ex.getMessage());
                        plugin.getConfigManager().debug("WebSocket error details: " + ex);
                    });
                }
            };

            webSocketClient.connect();
            plugin.getConfigManager().debug("Connecting to WebSocket server: " + serverUrl);

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create WebSocket connection: " + e.getMessage());
            if (plugin.getConfigManager().isAutoConnect()) {
                scheduleReconnect();
            }
        }
    }

    /**
     * Disconnect from the WebSocket server
     */
    public void disconnect() {
        stopHeartbeat();
        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception e) {
                plugin.getConfigManager().debug("Error closing WebSocket: " + e.getMessage());
            }
            webSocketClient = null;
        }
        connected = false;
        reconnectAttempts = 0;
        plugin.getLogger().info("Disconnected from InterConnect-Server");
    }

    private void startHeartbeat() {
        if (heartbeatTaskId != -1) return;
        
        int interval = plugin.getConfigManager().getHeartbeatInterval();
        long ticks = (long) interval * 20L;
        
        heartbeatTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!isConnected()) {
                stopHeartbeat();
                return;
            }
            
            if (!isAlive) {
                plugin.getLogger().warning("WebSocket heartbeat timeout, reconnecting...");
                disconnect();
                if (plugin.getConfigManager().isAutoConnect()) {
                    scheduleReconnect();
                }
                return;
            }
            
            isAlive = false;
            try {
                webSocketClient.sendPing();
            } catch (Exception e) {
                plugin.getConfigManager().debug("Failed to send ping: " + e.getMessage());
            }
        }, ticks, ticks);
    }

    private void stopHeartbeat() {
        if (heartbeatTaskId != -1) {
            Bukkit.getScheduler().cancelTask(heartbeatTaskId);
            heartbeatTaskId = -1;
        }
    }

    /**
     * Check if currently connected to the WebSocket server
     * 
     * @return true if connected
     */
    public boolean isConnected() {
        return connected && webSocketClient != null && webSocketClient.isOpen();
    }

    /**
     * Send a message to the WebSocket server
     * 
     * @param type The event type
     * @param targetId Target UUID or "all" for broadcast
     * @param messageData The message data object
     * @return true if message was sent successfully
     */
    public boolean sendMessage(String type, String targetId, JSONObject messageData) {
        if (!isConnected()) {
            plugin.getConfigManager().debug("Cannot send message: not connected");
            return false;
        }

        try {
            JSONObject packet = new JSONObject();
            packet.put("type", type);
            packet.put("targetId", targetId);
            packet.put("msg", messageData);

            String jsonString = packet.toString();
            webSocketClient.send(jsonString);
            
            plugin.getConfigManager().debug("Sent message: " + jsonString);
            return true;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send message: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send a broadcast message to all connected servers
     * 
     * @param type The event type
     * @param messageData The message data object
     * @return true if message was sent successfully
     */
    public boolean broadcast(String type, JSONObject messageData) {
        return sendMessage(type, "all", messageData);
    }

    /**
     * Handle incoming messages from other servers
     * 
     * @param message The JSON message string
     */
    private void handleIncomingMessage(String message) {
        try {
            JSONObject packet = new JSONObject(message);
            
            String fromId = packet.optString("fromId", "unknown");
            String fromName = packet.optString("fromName", "Unknown Server");
            String type = packet.optString("type", "");
            JSONObject msgData = packet.optJSONObject("msg");
            long time = packet.optLong("time", System.currentTimeMillis());

            plugin.getConfigManager().debug("Received message from " + fromName + " [" + fromId + "]: " + type);

            // Notify custom listeners first
            notifyMessageListeners(fromName, fromId, type, msgData);

            // Skip if it's a built-in event type
            if (BUILTIN_EVENTS.contains(type)) {
                // Handle built-in events
                switch (type) {
                    case EVENT_PLAYER_JOIN:
                        handlePlayerJoin(fromName, msgData);
                        break;
                    case EVENT_PLAYER_QUIT:
                        handlePlayerQuit(fromName, msgData);
                        break;
                    case EVENT_PLAYER_DEATH:
                        handlePlayerDeath(fromName, msgData);
                        break;
                    case EVENT_PLAYER_CHAT:
                    case EVENT_PLAYER_MESSAGE:
                        handlePlayerMessage(fromName, msgData);
                        break;
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse incoming message: " + e.getMessage());
            plugin.getConfigManager().debug("Raw message: " + message);
        }
    }

    /**
     * Notify all registered message listeners
     */
    private void notifyMessageListeners(String fromServer, String fromUuid, String eventType, JSONObject data) {
        for (MessageListener listener : messageListeners) {
            try {
                listener.onMessageReceived(fromServer, fromUuid, eventType, data);
            } catch (Exception e) {
                plugin.getLogger().warning("Error in message listener: " + e.getMessage());
            }
        }
    }

    /**
     * Register a message listener
     * 
     * @param listener The listener to register
     */
    public void registerMessageListener(MessageListener listener) {
        messageListeners.add(listener);
        // Sort by priority (higher first)
        messageListeners.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        plugin.getConfigManager().debug("Registered message listener: " + listener.getClass().getName());
    }

    /**
     * Unregister a message listener
     * 
     * @param listener The listener to unregister
     */
    public void unregisterMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
        plugin.getConfigManager().debug("Unregistered message listener: " + listener.getClass().getName());
    }

    /**
     * Handle player join event from another server
     */
    private void handlePlayerJoin(String fromServer, JSONObject data) {
        if (data == null) return;
        String playerName = data.optString("playerName", "Unknown");
        String message = plugin.getMessageManager().getMessage("cross-server.player-join",
            "server", fromServer,
            "player", playerName
        );
        Bukkit.broadcastMessage(message);
    }

    /**
     * Handle player quit event from another server
     */
    private void handlePlayerQuit(String fromServer, JSONObject data) {
        if (data == null) return;
        String playerName = data.optString("playerName", "Unknown");
        String message = plugin.getMessageManager().getMessage("cross-server.player-quit",
            "server", fromServer,
            "player", playerName
        );
        Bukkit.broadcastMessage(message);
    }

    /**
     * Handle player death event from another server
     */
    private void handlePlayerDeath(String fromServer, JSONObject data) {
        if (data == null) return;
        String playerName = data.optString("playerName", "Unknown");
        String deathMessage = data.optString("deathMessage", playerName + " died");
        String message = plugin.getMessageManager().getMessage("cross-server.player-death",
            "server", fromServer,
            "message", deathMessage
        );
        Bukkit.broadcastMessage(message);
    }

    /**
     * Handle player message/chat event from another server
     */
    private void handlePlayerMessage(String fromServer, JSONObject data) {
        if (data == null) return;
        String playerName = data.optString("playerName", "Unknown");
        String text = data.optString("text", "");
        String message = plugin.getMessageManager().getMessage("cross-server.player-chat",
            "server", fromServer,
            "player", playerName,
            "message", text
        );
        Bukkit.broadcastMessage(message);
    }

    /**
     * Schedule a reconnection attempt
     */
    private void scheduleReconnect() {
        int maxAttempts = plugin.getConfigManager().getReconnectMaxAttempts();
        
        if (reconnectAttempts >= maxAttempts && maxAttempts > 0) {
            plugin.getLogger().warning("Max reconnection attempts reached. Giving up.");
            return;
        }

        reconnectAttempts++;
        int interval = plugin.getConfigManager().getReconnectInterval();
        
        plugin.getLogger().info("Attempting to reconnect in " + interval + " seconds... (Attempt " + reconnectAttempts + ")");

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!connected) {
                connect();
            }
        }, interval * 20L); // Convert seconds to ticks
    }

    /**
     * Get current connection status
     * 
     * @return A formatted status string
     */
    public String getStatusString() {
        if (isConnected()) {
            return "§aConnected §7(to " + plugin.getConfigManager().getServerUrl() + ")";
        } else {
            return "§cDisconnected";
        }
    }
}
