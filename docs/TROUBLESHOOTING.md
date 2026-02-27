# Troubleshooting Guide

## Common Issues and Solutions

### Connection Issues

#### "Cannot connect: Server UUID or Token is not configured!"

**Cause**: The plugin is missing required authentication credentials.

**Solution**:
1. Register your server with InterConnect-Server:
   ```bash
   npm run start add-node YourServerName
   ```
2. Copy the UUID and Token into `config.yml`
3. Restart the server

#### "Failed to create WebSocket connection"

**Cause**: Cannot establish connection to InterConnect-Server.

**Solution**:
1. Verify InterConnect-Server is running
2. Check the URL in config.yml
3. Test network connectivity:
   ```bash
   curl http://interconnect-server:8000/
   ```
4. Check firewall settings (port 8000)

#### Connection keeps dropping

**Cause**: Network instability or server overload.

**Solution**:
1. Enable debug mode in config.yml
2. Check server logs for specific errors
3. Increase reconnect-interval:
   ```yaml
   connection:
     reconnect-interval: 10
   ```
4. Check InterConnect-Server capacity

### Authentication Issues

#### "WS rejected – invalid uuid or token"

**Cause**: Wrong credentials or node was reset.

**Solution**:
1. Verify UUID and token match InterConnect-Server records
2. Re-register the node if credentials were lost
3. Ensure no extra spaces in config.yml

#### "WS rejected – uuid already connected"

**Cause**: Same UUID connecting from multiple servers.

**Solution**:
1. Each Minecraft server needs unique credentials
2. Register separate nodes for each server

### Message Sync Issues

#### Events not appearing on other servers

**Cause**: Connection issues or configuration.

**Solution**:
1. Check connection: `/ic status`
2. Verify event sync is enabled:
   ```yaml
   events:
     sync-player-join: true
     sync-player-quit: true
   ```
3. Enable debug mode to see messages
4. Check all servers have unique UUIDs

#### Messages appearing multiple times

**Cause**: Multiple connections or misconfiguration.

**Solution**:
1. Ensure only one instance of plugin is running
2. Check for duplicate entries in InterConnect-Server
3. Restart all servers

### Performance Issues

#### Server lag when plugin is enabled

**Cause**: Network latency or message flooding.

**Solution**:
1. Enable debug mode to check message frequency
2. Disable unnecessary event sync:
   ```yaml
   events:
     sync-player-chat: false  # If not needed
   ```
3. Check InterConnect-Server latency

#### High CPU usage

**Cause**: Too many reconnection attempts.

**Solution**:
1. Check connection stability
2. Adjust reconnect settings:
   ```yaml
   connection:
     reconnect-interval: 30
     reconnect-max-attempts: 5
   ```

## Debug Mode

Enable debug mode to see detailed logs:

```yaml
connection:
  debug-mode: true
```

This will show:
- All sent and received messages
- Connection state changes
- Authentication details (masked)

## Getting Help

### Collecting Information

When reporting issues, include:

1. **Plugin version**: `/ic version`
2. **Server info**: `/version`
3. **Config** (remove sensitive data):
   ```yaml
   server:
     url: "ws://..."
     name: "ServerName"
     # Don't share uuid/token!
   ```
4. **Relevant logs**:
   - Enable debug mode
   - Reproduce the issue
   - Collect logs from startup

### Support Channels

1. **GitHub Issues**: [Create an issue](../../issues)
2. **Discord**: (coming soon)

## FAQ

### Q: Can I use this with BungeeCord/Velocity?

A: Yes! InterConnect works alongside proxy systems. Each backend server connects independently.

### Q: Does this work with modded servers (Forge/Fabric)?

A: This plugin is for Spigot/Paper only. For modded servers, you'd need a different implementation.

### Q: How many servers can connect?

A: Limited by your InterConnect-Server capacity. Typically 50+ servers work fine.

### Q: Is the connection encrypted?

A: Only if you use wss:// (WebSocket Secure). ws:// is unencrypted.

### Q: Can I disable specific events?

A: Yes, edit events section in config.yml:
```yaml
events:
  sync-player-chat: false  # Disable chat sync
```

### Q: Will this cause lag?

A: Minimal impact. Messages are sent asynchronously. Disable unnecessary events if concerned.
