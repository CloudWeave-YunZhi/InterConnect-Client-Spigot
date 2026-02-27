/*
 * Example Plugin demonstrating InterConnect API usage
 * 
 * This is a complete example showing how to use the InterConnect API
 * to send and receive messages between Minecraft servers.
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
 * Example plugin showing InterConnect integration
 */
public class ExamplePlugin extends JavaPlugin implements Listener, MessageListener {

    private InterConnectAPI interConnect;
    
    @Override
    public void onEnable() {
        // Get the InterConnect API instance
        interConnect = InterConnectAPI.getInstance();
        
        if (interConnect == null) {
            getLogger().warning("InterConnect is not installed! This plugin requires InterConnect.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getLogger().info("InterConnect found! Version: " + interConnect.getVersion());
        
        // Register as a message listener to receive cross-server messages
        interConnect.registerMessageListener(this);
        
        // Register Bukkit events
        getServer().getPluginManager().registerEvents(this, this);
        
        getLogger().info("ExamplePlugin enabled!");
    }
    
    @Override
    public void onDisable() {
        if (interConnect != null) {
            // Unregister the listener
            interConnect.unregisterMessageListener(this);
        }
        getLogger().info("ExamplePlugin disabled!");
    }
    
    /**
     * Handle incoming messages from other servers
     */
    @Override
    public void onMessageReceived(String fromServer, String fromUuid, 
                                  String eventType, JSONObject data) {
        
        getLogger().info("Received message from " + fromServer + 
                        " [" + fromUuid + "]: " + eventType);
        
        // Handle custom teleport request
        if ("example_teleport".equals(eventType)) {
            String playerName = data.optString("playerName", "Unknown");
            String targetLocation = data.optString("target", "unknown");
            
            getLogger().info("Player " + playerName + " wants to teleport to " + 
                           targetLocation + " from server " + fromServer);
            
            // You would implement actual teleport logic here
        }
        
        // Handle custom broadcast
        if ("example_broadcast".equals(eventType)) {
            String message = data.optString("message", "");
            String sender = data.optString("sender", "Unknown");
            
            // Broadcast to all players on this server
            getServer().broadcastMessage("§7[§b" + fromServer + "§7] §f" + 
                                       sender + "§7: §f" + message);
        }
    }
    
    /**
     * Set message listener priority (higher = called first)
     */
    @Override
    public int getPriority() {
        return 10;
    }
    
    /**
     * Example: Send a message when a player joins
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (interConnect == null || !interConnect.isConnected()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Send a custom event to all connected servers
        JSONObject data = new JSONObject();
        data.put("playerName", player.getName());
        data.put("uuid", player.getUniqueId().toString());
        data.put("firstJoin", !player.hasPlayedBefore());
        
        boolean sent = interConnect.broadcastJson("example_player_join", data);
        
        if (sent) {
            getLogger().info("Broadcast join event for " + player.getName());
        }
    }
    
    /**
     * Example commands
     */
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
            case "requestteleport":
                return handleTeleportCommand(sender, args);
            case "icstatus":
                return handleStatusCommand(sender);
            default:
                return false;
        }
    }
    
    /**
     * Broadcast a message to all servers
     */
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
        String senderName = sender instanceof Player ? 
                           ((Player) sender).getName() : "Console";
        
        JSONObject data = new JSONObject();
        data.put("message", message);
        data.put("sender", senderName);
        
        boolean sent = interConnect.broadcastJson("example_broadcast", data);
        
        if (sent) {
            sender.sendMessage("§aMessage broadcasted to all servers!");
        } else {
            sender.sendMessage("§cFailed to broadcast message.");
        }
        
        return true;
    }
    
    /**
     * Request a teleport to another server's location
     */
    private boolean handleTeleportCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /requestteleport <target>");
            sender.sendMessage("§cExample: /requestteleport spawn");
            return true;
        }
        
        if (!interConnect.isConnected()) {
            sender.sendMessage("§cNot connected to InterConnect network!");
            return true;
        }
        
        Player player = (Player) sender;
        String target = args[0];
        
        JSONObject data = new JSONObject();
        data.put("playerName", player.getName());
        data.put("playerUuid", player.getUniqueId().toString());
        data.put("target", target);
        data.put("requestTime", System.currentTimeMillis());
        
        boolean sent = interConnect.broadcastJson("example_teleport", data);
        
        if (sent) {
            player.sendMessage("§aTeleport request sent to all servers!");
        } else {
            player.sendMessage("§cFailed to send teleport request.");
        }
        
        return true;
    }
    
    /**
     * Check InterConnect status
     */
    private boolean handleStatusCommand(CommandSender sender) {
        sender.sendMessage("§6=== InterConnect Status ===");
        sender.sendMessage("§eConnected: " + (interConnect.isConnected() ? "§aYes" : "§cNo"));
        sender.sendMessage("§eServer Name: §f" + interConnect.getServerName());
        sender.sendMessage("§ePlugin Version: §f" + interConnect.getVersion());
        
        if (interConnect.isConnected()) {
            // Send a test message
            JSONObject testData = new JSONObject();
            testData.put("test", true);
            testData.put("timestamp", System.currentTimeMillis());
            
            boolean testSent = interConnect.broadcastJson("example_test", testData);
            sender.sendMessage("§eTest message: " + (testSent ? "§aSent" : "§cFailed"));
        }
        
        return true;
    }
}
