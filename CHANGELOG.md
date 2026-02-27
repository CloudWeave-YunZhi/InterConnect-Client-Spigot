# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

## [1.0.0] - 2024-02-27

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

[Unreleased]: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot/releases/tag/v1.0.0
