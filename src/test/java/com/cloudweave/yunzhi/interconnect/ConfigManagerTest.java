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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigManager
 */
public class ConfigManagerTest {

    private InterConnectPlugin plugin;
    private FileConfiguration config;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        plugin = mock(InterConnectPlugin.class);
        config = new YamlConfiguration();
        
        // Setup default config values
        config.set("server.url", "ws://localhost:8000/ws");
        config.set("server.uuid", "test-uuid-123");
        config.set("server.token", "test-token-456");
        config.set("server.name", "TestServer");
        config.set("server.auto-connect", true);
        config.set("events.sync-player-join", true);
        config.set("events.sync-player-quit", true);
        config.set("events.sync-player-death", true);
        config.set("events.sync-player-chat", true);
        config.set("events.sync-player-message", true);
        config.set("connection.reconnect-interval", 5);
        config.set("connection.reconnect-max-attempts", 10);
        config.set("connection.debug-mode", false);
        
        when(plugin.getConfig()).thenReturn(config);
        
        configManager = new ConfigManager(plugin);
        configManager.loadConfig();
    }

    @Test
    @DisplayName("Should load server URL correctly")
    void testGetServerUrl() {
        assertEquals("ws://localhost:8000/ws", configManager.getServerUrl());
    }

    @Test
    @DisplayName("Should load server UUID correctly")
    void testGetServerUuid() {
        assertEquals("test-uuid-123", configManager.getServerUuid());
    }

    @Test
    @DisplayName("Should load server token correctly")
    void testGetServerToken() {
        assertEquals("test-token-456", configManager.getServerToken());
    }

    @Test
    @DisplayName("Should load server name correctly")
    void testGetServerName() {
        assertEquals("TestServer", configManager.getServerName());
    }

    @Test
    @DisplayName("Should detect valid configuration")
    void testIsValid() {
        assertTrue(configManager.isValid());
    }

    @Test
    @DisplayName("Should detect invalid configuration when UUID is empty")
    void testIsValidWithEmptyUuid() {
        config.set("server.uuid", "");
        configManager.loadConfig();
        assertFalse(configManager.isValid());
    }

    @Test
    @DisplayName("Should detect invalid configuration when token is empty")
    void testIsValidWithEmptyToken() {
        config.set("server.token", "");
        configManager.loadConfig();
        assertFalse(configManager.isValid());
    }

    @Test
    @DisplayName("Should load auto-connect setting")
    void testIsAutoConnect() {
        assertTrue(configManager.isAutoConnect());
    }

    @Test
    @DisplayName("Should load event sync settings")
    void testEventSyncSettings() {
        assertTrue(configManager.isSyncPlayerJoin());
        assertTrue(configManager.isSyncPlayerQuit());
        assertTrue(configManager.isSyncPlayerDeath());
        assertTrue(configManager.isSyncPlayerChat());
        assertTrue(configManager.isSyncPlayerMessage());
    }

    @Test
    @DisplayName("Should load reconnect interval")
    void testGetReconnectInterval() {
        assertEquals(5, configManager.getReconnectInterval());
    }

    @Test
    @DisplayName("Should load reconnect max attempts")
    void testGetReconnectMaxAttempts() {
        assertEquals(10, configManager.getReconnectMaxAttempts());
    }

    @Test
    @DisplayName("Should use default values for missing config")
    void testDefaultValues() {
        FileConfiguration emptyConfig = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(emptyConfig);
        
        ConfigManager emptyConfigManager = new ConfigManager(plugin);
        emptyConfigManager.loadConfig();
        
        assertEquals("ws://localhost:8000/ws", emptyConfigManager.getServerUrl());
        assertEquals("", emptyConfigManager.getServerUuid());
        assertEquals("", emptyConfigManager.getServerToken());
        assertEquals("MyServer", emptyConfigManager.getServerName());
        assertTrue(emptyConfigManager.isAutoConnect());
        assertTrue(emptyConfigManager.isSyncPlayerJoin());
        assertEquals(5, emptyConfigManager.getReconnectInterval());
        assertEquals(10, emptyConfigManager.getReconnectMaxAttempts());
        assertFalse(emptyConfigManager.isDebugMode());
    }
}
