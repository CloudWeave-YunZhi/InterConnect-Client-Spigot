package com.cloudweave.yunzhi.interconnect.utils;

import com.cloudweave.yunzhi.interconnect.InterConnectPlugin;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final InterConnectPlugin plugin;
    private final String prefix;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Logger(InterConnectPlugin plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
    }

    public void info(String message) {
        plugin.getLogger().info(formatMessage(message));
    }

    public void info(String message, Object... args) {
        plugin.getLogger().info(formatMessage(String.format(message, args)));
    }

    public void warn(String message) {
        plugin.getLogger().warning(formatMessage(message));
    }

    public void warn(String message, Object... args) {
        plugin.getLogger().warning(formatMessage(String.format(message, args)));
    }

    public void error(String message) {
        plugin.getLogger().severe(formatMessage(message));
    }

    public void error(String message, Throwable throwable) {
        plugin.getLogger().severe(formatMessage(message));
        throwable.printStackTrace();
    }

    public void error(String message, Object... args) {
        plugin.getLogger().severe(formatMessage(String.format(message, args)));
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            plugin.getLogger().info(formatMessage("[DEBUG] " + message));
        }
    }

    public void debug(String message, Object... args) {
        if (isDebugEnabled()) {
            plugin.getLogger().info(formatMessage("[DEBUG] " + String.format(message, args)));
        }
    }

    private String formatMessage(String message) {
        return String.format("[%s] [%s] %s", 
            LocalDateTime.now().format(formatter), 
            prefix, 
            message);
    }

    private boolean isDebugEnabled() {
        return plugin.getConfigManager().isDebugMode();
    }
}
