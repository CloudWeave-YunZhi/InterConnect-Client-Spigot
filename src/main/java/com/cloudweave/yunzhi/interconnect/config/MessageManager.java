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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Message Manager for multi-language support
 * Handles loading and formatting of localized messages
 */
public class MessageManager {

    private final InterConnectPlugin plugin;
    private FileConfiguration messagesConfig;
    private String currentLanguage;
    
    // Cache for messages
    private final Map<String, String> messageCache = new HashMap<>();
    
    // Supported languages
    public static final String LANG_EN = "en";
    public static final String LANG_ZH_CN = "zh_CN";
    public static final String DEFAULT_LANGUAGE = LANG_ZH_CN;

    public MessageManager(InterConnectPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load messages configuration
     */
    public void loadMessages() {
        currentLanguage = plugin.getConfig().getString("language", DEFAULT_LANGUAGE);
        
        // Save default language files if they don't exist
        saveDefaultLanguageFile(LANG_EN);
        saveDefaultLanguageFile(LANG_ZH_CN);
        
        // Load the selected language file
        File langFile = new File(plugin.getDataFolder(), "lang/" + currentLanguage + ".yml");
        
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file " + currentLanguage + ".yml not found, using default: " + DEFAULT_LANGUAGE);
            currentLanguage = DEFAULT_LANGUAGE;
            langFile = new File(plugin.getDataFolder(), "lang/" + DEFAULT_LANGUAGE + ".yml");
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(langFile);
        
        // Load defaults from jar
        InputStream defaultStream = plugin.getResource("lang/" + currentLanguage + ".yml");
        if (defaultStream != null) {
            messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8)));
        }
        
        // Clear cache
        messageCache.clear();
        
        plugin.getConfigManager().debug("Loaded language: " + currentLanguage);
    }

    /**
     * Save default language file from resources
     */
    private void saveDefaultLanguageFile(String langCode) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        
        File langFile = new File(langFolder, langCode + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + langCode + ".yml", false);
        }
    }

    /**
     * Get a message by key
     * 
     * @param key The message key
     * @return The formatted message
     */
    public String getMessage(String key) {
        // Check cache first
        if (messageCache.containsKey(key)) {
            return messageCache.get(key);
        }
        
        String message = messagesConfig.getString(key);
        
        if (message == null) {
            message = "&cMissing message: " + key;
            plugin.getLogger().warning("Missing message key: " + key);
        }
        
        // Apply prefix
        String prefix = messagesConfig.getString("prefix", "&6[InterConnect] ");
        if (message.contains("%prefix%")) {
            message = message.replace("%prefix%", prefix);
        }
        
        // Colorize
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // Cache and return
        messageCache.put(key, message);
        return message;
    }

    /**
     * Get a message with placeholders replaced
     * 
     * @param key The message key
     * @param placeholders Placeholder key-value pairs
     * @return The formatted message
     */
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        
        // Replace placeholders
        if (placeholders.length % 2 == 0) {
            for (int i = 0; i < placeholders.length; i += 2) {
                String placeholder = placeholders[i];
                String value = placeholders[i + 1];
                message = message.replace("%" + placeholder + "%", value);
            }
        }
        
        return message;
    }

    /**
     * Send a message to a CommandSender
     * 
     * @param sender The recipient
     * @param key The message key
     */
    public void sendMessage(CommandSender sender, String key) {
        sender.sendMessage(getMessage(key));
    }

    /**
     * Send a message with placeholders to a CommandSender
     * 
     * @param sender The recipient
     * @param key The message key
     * @param placeholders Placeholder key-value pairs
     */
    public void sendMessage(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(getMessage(key, placeholders));
    }

    /**
     * Send a prefixed message
     * 
     * @param sender The recipient
     * @param message The raw message
     */
    public void sendPrefixedMessage(CommandSender sender, String message) {
        String prefix = messagesConfig.getString("prefix", "&6[InterConnect] ");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    /**
     * Get the current language code
     * 
     * @return The language code (e.g., "en", "zh_CN")
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Reload messages
     */
    public void reload() {
        loadMessages();
    }
}
