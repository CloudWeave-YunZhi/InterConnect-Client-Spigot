/*
 * InterConnect-Client-Spigot
 * Copyright (C) 2024 CloudWeave-YunZhi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.cloudweave.yunzhi.interconnect.update;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Update Checker for InterConnect Plugin
 * Checks for updates from GitHub releases
 */
public class UpdateChecker implements Listener {

    private final InterConnectPlugin plugin;
    private final String currentVersion;
    private String latestVersion;
    private boolean updateAvailable = false;
    private String downloadUrl;

    // GitHub API URL for latest release
    private static final String GITHUB_API_URL = "https://api.github.com/repos/CloudWeave-YunZhi/InterConnect-Client-Spigot/releases/latest";
    private static final String GITHUB_RELEASES_URL = "https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot/releases/latest";

    public UpdateChecker(InterConnectPlugin plugin) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.downloadUrl = GITHUB_RELEASES_URL;
        
        // Register join event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Check for updates asynchronously
     */
    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getConfigManager().debug("Checking for updates...");
                
                HttpsURLConnection connection = (HttpsURLConnection) new URL(GITHUB_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "InterConnect-UpdateChecker/" + currentVersion);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getConfigManager().debug("Update check failed: HTTP " + responseCode);
                    return;
                }
                
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                // Parse JSON response (simple parsing without external library)
                String json = response.toString();
                latestVersion = extractVersionFromJson(json);
                
                if (latestVersion == null) {
                    plugin.getConfigManager().debug("Could not parse version from GitHub API response");
                    return;
                }
                
                // Compare versions
                if (isNewerVersion(latestVersion, currentVersion)) {
                    updateAvailable = true;
                    plugin.getLogger().info("============================================");
                    plugin.getLogger().info("A new version of InterConnect is available!");
                    plugin.getLogger().info("Current version: " + currentVersion);
                    plugin.getLogger().info("Latest version: " + latestVersion);
                    plugin.getLogger().info("Download: " + downloadUrl);
                    plugin.getLogger().info("============================================");
                } else {
                    plugin.getConfigManager().debug("You are running the latest version: " + currentVersion);
                }
                
            } catch (IOException e) {
                plugin.getConfigManager().debug("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    /**
     * Extract version tag from GitHub API JSON response
     */
    private String extractVersionFromJson(String json) {
        // Look for "tag_name":"v1.0.0" pattern
        String searchKey = "\"tag_name\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) return null;
        
        String tag = json.substring(startIndex, endIndex);
        // Remove 'v' prefix if present
        if (tag.startsWith("v")) {
            tag = tag.substring(1);
        }
        return tag;
    }

    /**
     * Compare two version strings
     * 
     * @param newVersion The new version
     * @param currentVersion The current version
     * @return true if newVersion is newer than currentVersion
     */
    private boolean isNewerVersion(String newVersion, String currentVersion) {
        String[] newParts = newVersion.split("\\.");
        String[] currentParts = currentVersion.split("\\.");
        
        int maxLength = Math.max(newParts.length, currentParts.length);
        
        for (int i = 0; i < maxLength; i++) {
            int newPart = i < newParts.length ? parseVersionPart(newParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
            
            if (newPart > currentPart) {
                return true;
            } else if (newPart < currentPart) {
                return false;
            }
        }
        
        return false; // Versions are equal
    }

    /**
     * Parse a version part, handling pre-release suffixes
     */
    private int parseVersionPart(String part) {
        // Handle parts like "0-SNAPSHOT" or "1-beta"
        String numericPart = part.replaceAll("[^0-9].*$", "");
        try {
            return numericPart.isEmpty() ? 0 : Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Check if an update is available
     * 
     * @return true if a newer version is available
     */
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Get the latest version
     * 
     * @return The latest version string
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Get the current version
     * 
     * @return The current version string
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Get the download URL
     * 
     * @return The download URL
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * Notify admins when they join if an update is available
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("interconnect.admin")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage("§6[InterConnect] §eA new version is available: §f" + latestVersion);
                    player.sendMessage("§6[InterConnect] §eYou are running: §f" + currentVersion);
                    player.sendMessage("§6[InterConnect] §eDownload: §f" + downloadUrl);
                }
            }, 60L); // Delay 3 seconds
        }
    }
}
