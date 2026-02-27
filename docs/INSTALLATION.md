# Installation Guide

## Prerequisites

- Minecraft Server running Spigot, Paper, or Purpur 1.13+
- Java 8 or higher
- InterConnect-Server instance running

## Installation Steps

### 1. Download the Plugin

Download the latest release from [GitHub Releases](../../releases).

### 2. Install the Plugin

1. Stop your Minecraft server
2. Place the downloaded `ICC-Spigot-1.0.0.jar` file into your server's `plugins/` folder
3. Start your server

### 3. Configure the Plugin

1. After the first start, a configuration file will be created at `plugins/InterConnect-Client-Spigot/config.yml`
2. Stop the server
3. Edit the configuration file (see [Configuration](#configuration))
4. Start the server again

## Configuration

### Getting UUID and Token

Before configuring the plugin, you need to register your server with the InterConnect-Server.

#### Option 1: Using CLI (Recommended)

On the InterConnect-Server machine:

```bash
cd InterConnect-Server
npm run start add-node MyMinecraftServer
```

This will output something like:
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "token": "mysecrettoken123"
}
```

#### Option 2: Using REST API

```bash
curl -X POST http://localhost:8000/manager/keys/MyMinecraftServer \
  -H "X-Admin-Token: <your_admin_password_sha256>"
```

Response:
```json
{
  "success": true,
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "token": "mysecrettoken123"
  }
}
```

### Editing config.yml

Edit `plugins/InterConnect-Client-Spigot/config.yml`:

```yaml
server:
  # WebSocket URL of your InterConnect-Server
  url: "ws://your.interconnect.server:8000/ws"
  
  # UUID from the registration
  uuid: "550e8400-e29b-41d4-a716-446655440000"
  
  # Token from the registration
  token: "mysecrettoken123"
  
  # Your server's display name
  name: "MyMinecraftServer"
```

### Language Configuration

The plugin supports multiple languages:

```yaml
language: zh_CN  # or "en"
```

Available languages:
- `zh_CN` - Simplified Chinese (简体中文)
- `en` - English

You can customize messages by editing files in `plugins/InterConnect-Client-Spigot/lang/`.

## Verification

After configuration, start your server and check the console:

```
[InterConnect] Successfully connected to InterConnect-Server!
```

You can also use the in-game command:

```
/ic status
```

This will display your connection status.

## Troubleshooting

### Connection Failed

**Problem**: Plugin shows "Cannot connect: Server UUID or Token is not configured!"

**Solution**: 
- Make sure you've set both `uuid` and `token` in config.yml
- Verify there are no extra spaces

### Cannot Connect to Server

**Problem**: Plugin shows "Failed to create WebSocket connection"

**Solution**:
1. Check that the InterConnect-Server is running
2. Verify the URL in config.yml is correct
3. Check firewall settings - port 8000 must be open
4. Test connection: `curl http://your.server:8000/`

### Authentication Failed

**Problem**: Connection closes immediately with "Unauthorized"

**Solution**:
- Verify UUID and token are correct
- Check if the node was deleted from InterConnect-Server
- Re-register the node if necessary

### Events Not Syncing

**Problem**: Player joins/quits are not showing on other servers

**Solution**:
1. Check connection status: `/ic status`
2. Verify event sync is enabled in config.yml
3. Check that all servers have unique UUIDs
4. Enable debug mode to see detailed logs

## Updating the Plugin

1. Stop your server
2. Backup your `config.yml`
3. Replace the old jar with the new one
4. Start the server
5. Check for new configuration options in the updated config.yml

## Uninstallation

1. Stop your server
2. Remove `ICC-Spigot-*.jar` from `plugins/`
3. Remove `plugins/InterConnect-Client-Spigot/` folder (optional, removes configs)
4. Start your server
