# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0.0 | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability within InterConnect-Client-Spigot, please send an email to the maintainers. **Do not** open a public issue.

### Security Contact

- GitHub Security Advisory: [Report a vulnerability](../../security/advisories/new)

### What to Include

When reporting a vulnerability, please include:

- Description of the vulnerability
- Steps to reproduce (if applicable)
- Potential impact
- Suggested fix (if any)

### Response Time

We aim to respond to security reports within 48 hours. You can expect:

1. **Initial Response** (within 48 hours): Acknowledgment of receipt
2. **Assessment** (within 1 week): Evaluation of the vulnerability
3. **Resolution** (timeline varies): Fix and disclosure

## Security Best Practices

### For Server Administrators

1. **Keep tokens secret**: Your InterConnect token is like a password. Never share it or commit it to version control.

2. **Use HTTPS/WSS**: When possible, use encrypted WebSocket connections (wss://) instead of ws://

3. **Restrict access**: Limit who can execute `/ic` commands through proper permission management

4. **Regular updates**: Keep the plugin updated to receive security patches

5. **Monitor logs**: Watch for unusual connection attempts or authentication failures

### For Developers

1. **Validate inputs**: Always validate data received from other servers

2. **Sanitize messages**: Don't trust messages from other servers blindly

3. **Use the API**: When extending functionality, use the provided API methods

4. **Report issues**: If you find security issues, report them privately

## Known Security Considerations

### Token Storage

- Tokens are stored in plain text in `config.yml`
- Ensure your server files have appropriate permissions
- Consider using environment variables for sensitive data (future feature)

### WebSocket Connection

- By default, connections are not encrypted (ws://)
- Use wss:// for production environments
- Be aware that messages are broadcast to all connected nodes

### Message Content

- Player chat messages are forwarded as-is
- Consider implementing content filtering if needed
- Death messages and other event data may contain user-generated content

## Security History

| Date | Version | Issue | Status |
|------|---------|-------|--------|
| N/A  | -       | No security issues reported yet | - |
