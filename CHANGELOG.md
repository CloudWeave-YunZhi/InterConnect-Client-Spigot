# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.1.0] - 2026-03-19

### Changed
- Aligned the Spigot client with the current InterConnect-Server WebSocket protocol
  - Reject unsupported outbound event types on the client side
  - Ignore server heartbeat packets instead of exposing them to plugin listeners
  - Clarify node naming semantics in commands, config comments, and API docs

### Documentation
- Updated release references and build artifact names from `1.0.1` to `1.1.0`
- Refreshed API examples and installation guidance to match the current protocol behavior

## [1.0.1] - 2026-03-15

### Fixed
- Fixed WebSocket auto-reconnection failure after disconnection
  - Clear `webSocketClient` reference in `onClose` callback to ensure `connect()` works properly on next attempt
  - Strengthen double-check in `scheduleReconnect()` to prevent "Already connected or connecting!" errors in race conditions

### Changed
- Cleaned up unused imports and variables
  - Removed unused `Player` and `IOException` imports in `MessageManager`
  - Removed unused `plugin` field in `PlayerEventListener`
  - Removed unused `ConcurrentHashMap` import and `time` variable in `WebSocketManager`

## [1.0.0] - 2026-02-27

### Added
- Initial plugin structure
- WebSocket client for connecting to InterConnect-Server
- Player event synchronization (join, quit, death, chat)
- Configuration system with auto-reload
- Command system with /ic commands
- Automatic reconnection with configurable intervals
- Debug mode for troubleshooting
- Multi-language support framework
- bStats metrics integration
- Update checker

### Added
- First stable release
- WebSocket connection to InterConnect-Server
- Support for events: player_join, player_quit, player_death, player_chat, player_message
- Configuration options for all event types
- Auto-connect on plugin enable
- Manual connect/disconnect commands
- Connection status display
- API for other plugins
- Permission system (interconnect.admin)
- Tab completion for commands
- Comprehensive logging

### Security
- UUID and Token-based authentication
- Secure header transmission
- Configurable reconnect limits

[1.1.0]: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot/compare/v1.0.1...v1.1.0
[1.0.1]: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot/releases/tag/v1.0.0
