/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.commands;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import com.cloudweave.yunzhi.interconnect.utils.Logger;
import com.cloudweave.yunzhi.interconnect.websocket.WebSocketManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InterConnectCommand implements CommandExecutor, TabCompleter {

    private final InterConnectPlugin plugin;
    private final Logger logger;
    private static final String PERMISSION_ADMIN = "interconnect.admin";

    public InterConnectCommand(InterConnectPlugin plugin) {
        this.plugin = plugin;
        this.logger = new Logger(plugin, "InterConnectCommand");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            sender.sendMessage("§cRequired: §7" + PERMISSION_ADMIN);
            return true;
        }

        // No arguments - show help
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "test":
                handleTest(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "help":
            case "?":
                sendHelp(sender);
                break;
            case "connect":
                handleConnect(sender);
                break;
            case "disconnect":
                handleDisconnect(sender);
                break;
            case "version":
                handleVersion(sender);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand: §7" + subCommand);
                sender.sendMessage("§cUse §7/ic help §cfor available commands.");
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            sender.sendMessage("§6[InterConnect] §eReloading configuration...");
            logger.info("Configuration reload requested by %s", sender.getName());
            
            plugin.reload();
            
            sender.sendMessage("§6[InterConnect] §aConfiguration reloaded successfully!");
            logger.info("Configuration reloaded successfully");
            
            boolean connected = plugin.getWebSocketManager().isConnected();
            if (connected) {
                sender.sendMessage("§6[InterConnect] §aWebSocket connection is active.");
            } else {
                sender.sendMessage("§6[InterConnect] §cWebSocket is disconnected.");
                sender.sendMessage("§6[InterConnect] §eUse §7/ic connect §eto connect.");
            }
        } catch (Exception e) {
            sender.sendMessage("§6[InterConnect] §cError reloading configuration!");
            logger.error("Error reloading configuration: %s", e.getMessage());
        }
    }

    private void handleTest(CommandSender sender) {
        try {
            sender.sendMessage("§6[InterConnect] §eTesting connection to InterConnect-Server...");
            logger.info("Connection test requested by %s", sender.getName());
            
            WebSocketManager wsManager = plugin.getWebSocketManager();
            
            if (!wsManager.isConnected()) {
                sender.sendMessage("§6[InterConnect] §cNot connected to server!");
                sender.sendMessage("§6[InterConnect] §eUse §7/ic connect §efirst.");
                logger.warn("Connection test failed: not connected");
                return;
            }

            JSONObject testData = new JSONObject();
            testData.put("playerName", sender.getName());
            testData.put("text", "Connection test from " + plugin.getConfigManager().getServerName());
            
            boolean success = wsManager.broadcast("player_message", testData);
            
            if (success) {
                sender.sendMessage("§6[InterConnect] §aTest message sent successfully!");
                sender.sendMessage("§6[InterConnect] §7If other servers are connected, they should receive this test message.");
                logger.info("Connection test successful");
            } else {
                sender.sendMessage("§6[InterConnect] §cFailed to send test message!");
                logger.warn("Connection test failed: message send failed");
            }
        } catch (Exception e) {
            sender.sendMessage("§6[InterConnect] §cError during connection test!");
            logger.error("Error during connection test: %s", e.getMessage());
        }
    }

    /**
     * Handle status subcommand
     */
    private void handleStatus(CommandSender sender) {
        sender.sendMessage("§6========== §bInterConnect Status §6==========");
        sender.sendMessage("");
        
        // Plugin info
        sender.sendMessage("§ePlugin Version: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§eServer Name: §f" + plugin.getConfigManager().getServerName());
        sender.sendMessage("");
        
        // Connection status
        sender.sendMessage("§eConnection Status:");
        sender.sendMessage("  " + plugin.getWebSocketManager().getStatusString());
        
        WebSocketManager wsManager = plugin.getWebSocketManager();
        if (wsManager.isConnected()) {
            sender.sendMessage("§eServer URL: §f" + plugin.getConfigManager().getServerUrl());
            sender.sendMessage("§eUUID: §f" + maskString(plugin.getConfigManager().getServerUuid()));
            sender.sendMessage("§eToken: §f" + maskString(plugin.getConfigManager().getServerToken()));
        }
        
        sender.sendMessage("");
        
        // Event sync settings
        sender.sendMessage("§eEvent Synchronization:");
        sender.sendMessage("  §7Player Join: " + formatBoolean(plugin.getConfigManager().isSyncPlayerJoin()));
        sender.sendMessage("  §7Player Quit: " + formatBoolean(plugin.getConfigManager().isSyncPlayerQuit()));
        sender.sendMessage("  §7Player Death: " + formatBoolean(plugin.getConfigManager().isSyncPlayerDeath()));
        sender.sendMessage("  §7Player Chat: " + formatBoolean(plugin.getConfigManager().isSyncPlayerChat()));
        sender.sendMessage("  §7Player Message: " + formatBoolean(plugin.getConfigManager().isSyncPlayerMessage()));
        
        sender.sendMessage("");
        sender.sendMessage("§6======================================");
    }

    private void handleConnect(CommandSender sender) {
        try {
            if (plugin.getWebSocketManager().isConnected()) {
                sender.sendMessage("§6[InterConnect] §aAlready connected to InterConnect-Server!");
                return;
            }

            if (!plugin.getConfigManager().isValid()) {
                sender.sendMessage("§6[InterConnect] §cConfiguration is incomplete!");
                sender.sendMessage("§6[InterConnect] §ePlease set §7server.uuid §eand §7server.token §ein config.yml");
                logger.warn("Connection attempt failed: configuration incomplete");
                return;
            }

            sender.sendMessage("§6[InterConnect] §eConnecting to InterConnect-Server...");
            logger.info("Connection requested by %s", sender.getName());
            plugin.getWebSocketManager().connect();
        } catch (Exception e) {
            sender.sendMessage("§6[InterConnect] §cError connecting to server!");
            logger.error("Error connecting to server: %s", e.getMessage());
        }
    }

    private void handleDisconnect(CommandSender sender) {
        try {
            if (!plugin.getWebSocketManager().isConnected()) {
                sender.sendMessage("§6[InterConnect] §cNot connected to InterConnect-Server!");
                return;
            }

            sender.sendMessage("§6[InterConnect] §eDisconnecting from InterConnect-Server...");
            logger.info("Disconnection requested by %s", sender.getName());
            plugin.getWebSocketManager().disconnect();
            sender.sendMessage("§6[InterConnect] §aDisconnected successfully!");
        } catch (Exception e) {
            sender.sendMessage("§6[InterConnect] §cError disconnecting from server!");
            logger.error("Error disconnecting from server: %s", e.getMessage());
        }
    }

    /**
     * Handle version subcommand
     */
    private void handleVersion(CommandSender sender) {
        sender.sendMessage("§6[InterConnect] §bVersion: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§6[InterConnect] §bAuthor: §fCloudWeave-YunZhi");
        sender.sendMessage("§6[InterConnect] §bGitHub: §fhttps://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot");
        sender.sendMessage("§6[InterConnect] §bLicense: §fGNU GPL v3.0");
    }

    /**
     * Send help message
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6========== §bInterConnect Commands §6==========");
        sender.sendMessage("");
        sender.sendMessage("§e/ic reload §7- Reload the configuration");
        sender.sendMessage("§e/ic test §7- Test API connectivity with server");
        sender.sendMessage("§e/ic status §7- Show connection and configuration status");
        sender.sendMessage("§e/ic connect §7- Connect to InterConnect-Server");
        sender.sendMessage("§e/ic disconnect §7- Disconnect from InterConnect-Server");
        sender.sendMessage("§e/ic version §7- Show plugin version information");
        sender.sendMessage("§e/ic help §7- Show this help message");
        sender.sendMessage("");
        sender.sendMessage("§6Permission: §7interconnect.admin");
        sender.sendMessage("§6======================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION_ADMIN)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String[] subCommands = {"reload", "test", "status", "connect", "disconnect", "version", "help"};
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            return completions;
        }

        return new ArrayList<>();
    }

    /**
     * Mask a string for display (show only first 4 and last 4 characters)
     */
    private String maskString(String input) {
        if (input == null || input.length() <= 8) {
            return "****";
        }
        return input.substring(0, 4) + "****" + input.substring(input.length() - 4);
    }

    /**
     * Format boolean value for display
     */
    private String formatBoolean(boolean value) {
        return value ? "§aEnabled" : "§cDisabled";
    }
}
