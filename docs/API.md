# InterConnect API Documentation

## Overview

InterConnect provides a public API that allows other plugins to interact with the WebSocket connection and send/receive custom messages across servers.

## Getting Started

### Obtaining the API Instance

```java
import com.cloudweave.yunzhi.interconnect.api.InterConnectAPI;

public class MyPlugin extends JavaPlugin {
    
    private InterConnectAPI interConnectAPI;
    
    @Override
    public void onEnable() {
        // Get the API instance
        interConnectAPI = InterConnectAPI.getInstance();
        
        if (interConnectAPI == null) {
            getLogger().warning("InterConnect is not installed or not enabled!");
            return;
        }
        
        // Check if connected
        if (interConnectAPI.isConnected()) {
            getLogger().info("Connected to InterConnect network as: " + interConnectAPI.getServerName());
        }
    }
}
```

## API Methods

### Connection Management

#### `isConnected()`
Check if connected to the InterConnect-Server.

```java
if (interConnectAPI.isConnected()) {
    // Do something
}
```

#### `connect()`
Manually connect to the InterConnect-Server.

```java
if (!interConnectAPI.isConnected()) {
    interConnectAPI.connect();
}
```

#### `disconnect()`
Disconnect from the InterConnect-Server.

```java
interConnectAPI.disconnect();
```

### Sending Messages

#### `broadcastMessage(String eventType, String message)`
Broadcast a simple text message to all connected servers.

```java
interConnectAPI.broadcastMessage("announcement", "Server will restart in 5 minutes!");
```

#### `broadcastJson(String eventType, JSONObject data)`
Broadcast a JSON object to all connected servers.

```java
import org.json.JSONObject;

JSONObject data = new JSONObject();
data.put("action", "warp");
data.put("target", "spawn");
data.put("player", player.getName());

interConnectAPI.broadcastJson("custom_warp", data);
```

#### `sendToServer(String targetUuid, String eventType, JSONObject data)`
Send a message to a specific server.

```java
String targetServerUuid = "550e8400-e29b-41d4-a716-446655440000";

JSONObject data = new JSONObject();
data.put("message", "Hello specific server!");

interConnectAPI.sendToServer(targetServerUuid, "private_message", data);
```

#### `broadcastPlayerEvent(String eventType, Player player, JSONObject additionalData)`
Broadcast a player-related event.

```java
JSONObject extra = new JSONObject();
extra.put("level", 50);
extra.put("class", "Warrior");

interConnectAPI.broadcastPlayerEvent("player_levelup", player, extra);
```

### Receiving Messages

#### `registerMessageListener(MessageListener listener)`
Register a listener to receive messages from other servers.

```java
import com.cloudweave.yunzhi.interconnect.api.MessageListener;
import org.json.JSONObject;

public class MyMessageListener implements MessageListener {
    
    @Override
    public void onMessageReceived(String fromServer, String fromUuid, 
                                  String eventType, JSONObject data) {
        
        getLogger().info("Received " + eventType + " from " + fromServer);
        
        if ("custom_warp".equals(eventType)) {
            String action = data.getString("action");
            String target = data.getString("target");
            String playerName = data.getString("player");
            
            // Handle the warp request
        }
    }
    
    @Override
    public int getPriority() {
        return 10; // Higher priority listeners are called first
    }
}

// Register the listener
interConnectAPI.registerMessageListener(new MyMessageListener());
```

#### `unregisterMessageListener(MessageListener listener)`
Unregister a previously registered listener.

```java
MyMessageListener listener = new MyMessageListener();
interConnectAPI.registerMessageListener(listener);

// Later...
interConnectAPI.unregisterMessageListener(listener);
```

### Utility Methods

#### `getServerName()`
Get the configured server name.

```java
String myServerName = interConnectAPI.getServerName();
```

#### `getVersion()`
Get the InterConnect plugin version.

```java
String version = interConnectAPI.getVersion();
```

#### `isDebugMode()` / `debug(String message)`
Check debug mode and log debug messages.

```java
if (interConnectAPI.isDebugMode()) {
    interConnectAPI.debug("This is a debug message");
}
```

## Complete Example

```java
package com.example.myplugin;

import com.cloudweave.yunzhi.interconnect.api.InterConnectAPI;
import com.cloudweave.yunzhi.interconnect.api.MessageListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

public class MyPlugin extends JavaPlugin implements MessageListener {
    
    private InterConnectAPI api;
    
    @Override
    public void onEnable() {
        api = InterConnectAPI.getInstance();
        
        if (api != null) {
            api.registerMessageListener(this);
            getLogger().info("Registered with InterConnect!");
        }
    }
    
    @Override
    public void onDisable() {
        if (api != null) {
            api.unregisterMessageListener(this);
        }
    }
    
    // Send a cross-server teleport request
    public void sendTeleportRequest(Player player, String targetServer) {
        if (api == null || !api.isConnected()) return;
        
        JSONObject data = new JSONObject();
        data.put("playerName", player.getName());
        data.put("playerUuid", player.getUniqueId().toString());
        data.put("targetServer", targetServer);
        
        api.broadcastJson("teleport_request", data);
    }
    
    // Handle incoming messages
    @Override
    public void onMessageReceived(String fromServer, String fromUuid, 
                                  String eventType, JSONObject data) {
        
        if ("teleport_request".equals(eventType)) {
            String playerName = data.getString("playerName");
            String targetServer = data.getString("targetServer");
            
            if (targetServer.equals(api.getServerName())) {
                getLogger().info("Player " + playerName + " wants to teleport here from " + fromServer);
                // Handle the teleport on this server
            }
        }
    }
    
    @Override
    public int getPriority() {
        return 5;
    }
}
```

## Event Types Reference

### Built-in Event Types

| Event Type | Description |
|-----------|-------------|
| `player_join` | Player joined a server |
| `player_quit` | Player left a server |
| `player_death` | Player died |
| `player_chat` | Player sent a chat message |
| `player_message` | Player sent a message |

### Custom Event Types

You can define your own event types. Use a prefix to avoid conflicts:
- Good: `myplugin_warp`, `myplugin_shop_purchase`
- Bad: `warp`, `purchase`

## Best Practices

1. **Always check for null**: InterConnectAPI.getInstance() may return null if the plugin is not loaded.

2. **Check connection status**: Always verify `isConnected()` before sending messages.

3. **Handle exceptions**: Wrap API calls in try-catch blocks to prevent your plugin from crashing.

4. **Use unique event types**: Prefix custom events with your plugin name to avoid conflicts.

5. **Clean up**: Unregister listeners in your plugin's `onDisable()` method.

6. **Don't block**: Message listeners are called on the main server thread. Offload heavy processing to async tasks.

## Troubleshooting

### API returns null
- Make sure InterConnect is installed and enabled
- Check your plugin's load order: `depend: [InterConnect-Client-Spigot]` in plugin.yml

### Messages not being received
- Verify both servers are connected to the InterConnect-Server
- Check that event types match exactly (case-sensitive)
- Enable debug mode to see received messages in console

### High CPU usage
- Avoid heavy processing in message listeners
- Use async tasks for database operations or HTTP requests
