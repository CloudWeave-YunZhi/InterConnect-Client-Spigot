# InterConnect API Documentation

## Overview

InterConnect exposes a public API for Spigot plugins that want to interact with the current InterConnect-Server WebSocket protocol.

The current server implementation forwards only these event types:

- `player_join`
- `player_quit`
- `player_death`
- `player_chat`
- `player_message`

If you try to send any other event type, the client now rejects it locally and returns `false`.

## Getting Started

```java
import com.cloudweave.yunzhi.interconnect.api.InterConnectAPI;

public class MyPlugin extends JavaPlugin {

    private InterConnectAPI interConnectAPI;

    @Override
    public void onEnable() {
        interConnectAPI = InterConnectAPI.getInstance();

        if (interConnectAPI == null) {
            getLogger().warning("InterConnect is not installed or not enabled!");
            return;
        }

        if (interConnectAPI.isConnected()) {
            getLogger().info("Connected to InterConnect network as: " + interConnectAPI.getServerName());
        }
    }
}
```

## Connection Management

### `isConnected()`

```java
if (interConnectAPI.isConnected()) {
    // Do something
}
```

### `connect()`

```java
if (!interConnectAPI.isConnected()) {
    interConnectAPI.connect();
}
```

### `disconnect()`

```java
interConnectAPI.disconnect();
```

## Sending Messages

### `isSupportedEventType(String eventType)`

Use this before sending if the event type may vary.

```java
if (interConnectAPI.isSupportedEventType("player_message")) {
    // Safe to send
}
```

### `broadcastMessage(String eventType, String message)`

This helper is for text-style events only. It currently supports:

- `player_message`
- `player_chat`

```java
interConnectAPI.broadcastMessage("player_message", "Server will restart in 5 minutes!");
```

### `broadcastJson(String eventType, JSONObject data)`

Use this when you need full control over the payload shape for one of the supported event types.

```java
import org.json.JSONObject;

JSONObject data = new JSONObject();
data.put("playerName", player.getName());
data.put("text", "Hello from another server");

interConnectAPI.broadcastJson("player_message", data);
```

### `sendToServer(String targetUuid, String eventType, JSONObject data)`

Send a supported event type to a specific node UUID.

```java
String targetServerUuid = "550e8400-e29b-41d4-a716-446655440000";

JSONObject data = new JSONObject();
data.put("playerName", "Console");
data.put("text", "Hello specific server!");

interConnectAPI.sendToServer(targetServerUuid, "player_message", data);
```

### `broadcastPlayerEvent(String eventType, Player player, JSONObject additionalData)`

Use this for supported player-related event types.

```java
JSONObject extra = new JSONObject();
extra.put("text", "Hello network");

interConnectAPI.broadcastPlayerEvent("player_message", player, extra);
```

## Receiving Messages

### `registerMessageListener(MessageListener listener)`

Listeners receive forwarded server events. Internal heartbeat packets are no longer exposed to listeners.

```java
import com.cloudweave.yunzhi.interconnect.api.MessageListener;
import org.json.JSONObject;

public class MyMessageListener implements MessageListener {

    @Override
    public void onMessageReceived(String fromServer, String fromUuid,
                                  String eventType, JSONObject data) {

        if ("player_message".equals(eventType)) {
            String playerName = data.optString("playerName", "Unknown");
            String text = data.optString("text", "");
            getLogger().info("[" + fromServer + "] " + playerName + ": " + text);
        }
    }

    @Override
    public int getPriority() {
        return 10;
    }
}

interConnectAPI.registerMessageListener(new MyMessageListener());
```

### `unregisterMessageListener(MessageListener listener)`

```java
MyMessageListener listener = new MyMessageListener();
interConnectAPI.registerMessageListener(listener);

// Later...
interConnectAPI.unregisterMessageListener(listener);
```

## Utility Methods

### `getServerName()`

Returns the plugin's configured node label. The actual source name seen by other servers is determined by the `servername` registered on InterConnect-Server for the current UUID/token.

### `getVersion()`

Returns the InterConnect plugin version.

### `isDebugMode()` / `debug(String message)`

Use these for plugin-side diagnostics.

## Example

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
        }
    }

    @Override
    public void onDisable() {
        if (api != null) {
            api.unregisterMessageListener(this);
        }
    }

    public void sendNetworkMessage(Player player, String text) {
        if (api == null || !api.isConnected()) {
            return;
        }

        JSONObject data = new JSONObject();
        data.put("playerName", player.getName());
        data.put("playerUuid", player.getUniqueId().toString());
        data.put("text", text);

        api.broadcastJson("player_message", data);
    }

    @Override
    public void onMessageReceived(String fromServer, String fromUuid,
                                  String eventType, JSONObject data) {
        if (!"player_message".equals(eventType)) {
            return;
        }

        String playerName = data.optString("playerName", "Unknown");
        String text = data.optString("text", "");
        getLogger().info("[" + fromServer + "] " + playerName + ": " + text);
    }

    @Override
    public int getPriority() {
        return 5;
    }
}
```

## Best Practices

1. Always null-check `InterConnectAPI.getInstance()`.
2. Check `isConnected()` before sending.
3. Validate dynamic event names with `isSupportedEventType(...)`.
4. Use payload keys that match the built-in event handlers.
5. Unregister listeners in `onDisable()`.
6. Keep message listeners lightweight because they run on the main thread.

## Troubleshooting

### API returns null

- Make sure InterConnect is installed and enabled.
- Add `depend: [InterConnect-Client-Spigot]` in your plugin's `plugin.yml`.

### Messages are not received

- Verify both servers are connected to InterConnect-Server.
- Verify the event type is one of the five supported types.
- Enable debug mode to inspect incoming packets.

### Message was sent but looked wrong on the receiving server

- Check that the payload keys match the event type.
- For `player_message` and `player_chat`, include `playerName` and `text`.
