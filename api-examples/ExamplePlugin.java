/*
 * Example Plugin demonstrating InterConnect API usage.
 *
 * This example only uses event types that are currently forwarded by
 * InterConnect-Server.
 */

package com.example.interconnectdemo;

import com.cloudweave.yunzhi.interconnect.api.InterConnectAPI;
import com.cloudweave.yunzhi.interconnect.api.MessageListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

/**
 * Example plugin showing InterConnect integration.
 */
public class ExamplePlugin extends JavaPlugin implements Listener, MessageListener {

    private InterConnectAPI interConnect;

    @Override
    public void onEnable() {
        interConnect = InterConnectAPI.getInstance();

        if (interConnect == null) {
            getLogger().warning("InterConnect is not installed! This plugin requires InterConnect.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        interConnect.registerMessageListener(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ExamplePlugin enabled with InterConnect " + interConnect.getVersion());
    }

    @Override
    public void onDisable() {
        if (interConnect != null) {
            interConnect.unregisterMessageListener(this);
        }
    }

    @Override
    public void onMessageReceived(String fromServer, String fromUuid,
                                  String eventType, JSONObject data) {
        if (!"player_message".equals(eventType)) {
            return;
        }

        String sender = data.optString("playerName", "Unknown");
        String text = data.optString("text", "");
        getLogger().info("Received from " + fromServer + " [" + fromUuid + "]: " + sender + " -> " + text);
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (interConnect == null || !interConnect.isConnected()) {
            return;
        }

        JSONObject data = new JSONObject();
        data.put("playerName", event.getPlayer().getName());
        data.put("text", event.getPlayer().getName() + " joined this server");

        boolean sent = interConnect.broadcastJson("player_message", data);
        if (sent) {
            getLogger().info("Broadcasted join notice for " + event.getPlayer().getName());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (interConnect == null) {
            sender.sendMessage("§cInterConnect is not available!");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "broadcastserver":
                return handleBroadcastCommand(sender, args);
            case "icstatus":
                return handleStatusCommand(sender);
            default:
                return false;
        }
    }

    private boolean handleBroadcastCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /broadcastserver <message>");
            return true;
        }

        if (!interConnect.isConnected()) {
            sender.sendMessage("§cNot connected to InterConnect network!");
            return true;
        }

        String message = String.join(" ", args);
        String senderName = sender instanceof Player
            ? ((Player) sender).getName()
            : "Console";

        JSONObject data = new JSONObject();
        data.put("playerName", senderName);
        data.put("text", message);

        boolean sent = interConnect.broadcastJson("player_message", data);
        sender.sendMessage(sent ? "§aMessage broadcasted to all servers!" : "§cFailed to broadcast message.");
        return true;
    }

    private boolean handleStatusCommand(CommandSender sender) {
        sender.sendMessage("§6=== InterConnect Status ===");
        sender.sendMessage("§eConnected: " + (interConnect.isConnected() ? "§aYes" : "§cNo"));
        sender.sendMessage("§eConfigured Node Name: §f" + interConnect.getServerName());
        sender.sendMessage("§ePlugin Version: §f" + interConnect.getVersion());

        if (interConnect.isConnected()) {
            boolean supported = interConnect.isSupportedEventType("player_message");
            sender.sendMessage("§eplayer_message supported: " + (supported ? "§aYes" : "§cNo"));
        }

        return true;
    }
}
