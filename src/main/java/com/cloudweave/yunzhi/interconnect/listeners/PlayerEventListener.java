/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.listeners;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import com.cloudweave.yunzhi.interconnect.config.ConfigManager;
import com.cloudweave.yunzhi.interconnect.utils.Logger;
import com.cloudweave.yunzhi.interconnect.websocket.WebSocketManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.JSONObject;

public class PlayerEventListener implements Listener {

    private final InterConnectPlugin plugin;
    private final WebSocketManager webSocketManager;
    private final ConfigManager configManager;
    private final Logger logger;

    public PlayerEventListener(InterConnectPlugin plugin) {
        this.plugin = plugin;
        this.webSocketManager = plugin.getWebSocketManager();
        this.configManager = plugin.getConfigManager();
        this.logger = new Logger(plugin, "PlayerEventListener");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configManager.isSyncPlayerJoin() || !webSocketManager.isConnected()) {
            return;
        }

        try {
            Player player = event.getPlayer();
            
            JSONObject data = new JSONObject();
            data.put("playerName", player.getName());
            data.put("uuid", player.getUniqueId().toString());
            data.put("displayName", player.getDisplayName());
            data.put("serverName", configManager.getServerName());
            
            logger.debug("Player joined: %s", player.getName());
            webSocketManager.broadcast(WebSocketManager.EVENT_PLAYER_JOIN, data);
        } catch (Exception e) {
            logger.error("Error handling player join event: %s", e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!configManager.isSyncPlayerQuit() || !webSocketManager.isConnected()) {
            return;
        }

        try {
            Player player = event.getPlayer();
            
            JSONObject data = new JSONObject();
            data.put("playerName", player.getName());
            data.put("uuid", player.getUniqueId().toString());
            data.put("displayName", player.getDisplayName());
            data.put("serverName", configManager.getServerName());
            
            logger.debug("Player quit: %s", player.getName());
            webSocketManager.broadcast(WebSocketManager.EVENT_PLAYER_QUIT, data);
        } catch (Exception e) {
            logger.error("Error handling player quit event: %s", e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configManager.isSyncPlayerDeath() || !webSocketManager.isConnected()) {
            return;
        }

        try {
            Player player = event.getEntity();
            String deathMessage = event.getDeathMessage();
            
            JSONObject data = new JSONObject();
            data.put("playerName", player.getName());
            data.put("uuid", player.getUniqueId().toString());
            data.put("deathMessage", deathMessage != null ? deathMessage : player.getName() + " died");
            data.put("serverName", configManager.getServerName());
            
            logger.debug("Player died: %s", player.getName());
            webSocketManager.broadcast(WebSocketManager.EVENT_PLAYER_DEATH, data);
        } catch (Exception e) {
            logger.error("Error handling player death event: %s", e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if ((!configManager.isSyncPlayerChat() && !configManager.isSyncPlayerMessage()) || !webSocketManager.isConnected()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        try {
            Player player = event.getPlayer();
            String message = event.getMessage();
            
            JSONObject data = new JSONObject();
            data.put("playerName", player.getName());
            data.put("uuid", player.getUniqueId().toString());
            data.put("displayName", player.getDisplayName());
            data.put("text", message);
            data.put("serverName", configManager.getServerName());
            
            logger.debug("Player chat: %s: %s", player.getName(), message);
            
            String eventType = configManager.isSyncPlayerChat() 
                ? WebSocketManager.EVENT_PLAYER_CHAT 
                : WebSocketManager.EVENT_PLAYER_MESSAGE;
            
            webSocketManager.broadcast(eventType, data);
        } catch (Exception e) {
            logger.error("Error handling player chat event: %s", e.getMessage());
        }
    }
}
