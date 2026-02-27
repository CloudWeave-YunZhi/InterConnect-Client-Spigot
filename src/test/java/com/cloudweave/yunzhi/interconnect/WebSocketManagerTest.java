/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect;

import com.cloudweave.yunzhi.interconnect.config.ConfigManager;
import com.cloudweave.yunzhi.interconnect.websocket.WebSocketManager;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketManager
 */
public class WebSocketManagerTest {

    private InterConnectPlugin plugin;
    private ConfigManager configManager;
    private Logger logger;
    private WebSocketManager webSocketManager;

    @BeforeEach
    void setUp() {
        plugin = mock(InterConnectPlugin.class);
        configManager = mock(ConfigManager.class);
        logger = mock(Logger.class);
        
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(plugin.getLogger()).thenReturn(logger);
        
        webSocketManager = new WebSocketManager(plugin);
    }

    @Test
    @DisplayName("Should not be connected initially")
    void testInitialConnectionState() {
        assertFalse(webSocketManager.isConnected());
    }

    @Test
    @DisplayName("Should return disconnected status string when not connected")
    void testDisconnectedStatusString() {
        String status = webSocketManager.getStatusString();
        assertTrue(status.contains("Disconnected"));
    }

    @Test
    @DisplayName("Should not send message when not connected")
    void testSendMessageWhenDisconnected() {
        JSONObject data = new JSONObject();
        data.put("test", "value");
        
        boolean result = webSocketManager.sendMessage("player_join", "all", data);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Should not broadcast when not connected")
    void testBroadcastWhenDisconnected() {
        JSONObject data = new JSONObject();
        data.put("playerName", "TestPlayer");
        
        boolean result = webSocketManager.broadcast("player_join", data);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("Event type constants should have correct values")
    void testEventTypeConstants() {
        assertEquals("player_join", WebSocketManager.EVENT_PLAYER_JOIN);
        assertEquals("player_quit", WebSocketManager.EVENT_PLAYER_QUIT);
        assertEquals("player_death", WebSocketManager.EVENT_PLAYER_DEATH);
        assertEquals("player_chat", WebSocketManager.EVENT_PLAYER_CHAT);
        assertEquals("player_message", WebSocketManager.EVENT_PLAYER_MESSAGE);
    }
}
