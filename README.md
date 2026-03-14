# InterConnect-Client-Spigot

一个用于连接 InterConnect-Server 的 Minecraft Spigot 插件，支持跨服玩家事件同步。

## 功能特性

- 🔗 **WebSocket 连接** - 与 InterConnect-Server 实时通信
- 📡 **事件广播** - 自动广播玩家加入、离开、死亡、聊天等事件到其他服务器
- 🔄 **自动重连** - 连接断开时自动尝试重连
- ⚙️ **灵活配置** - 可自定义同步的事件类型
- 🎮 **全版本兼容** - 支持 Spigot 1.13+ 所有版本

## 支持的事件类型

| 事件类型 | 说明 |
|---------|------|
| `player_join` | 玩家加入服务器 |
| `player_quit` | 玩家离开服务器 |
| `player_death` | 玩家死亡 |
| `player_chat` | 玩家聊天 |
| `player_message` | 玩家消息 |

## 安装方法

1. 从 [Releases](../../releases) 下载 `ICC-Spigot-1.0.1.jar`
2. 将 jar 文件放入服务器的 `plugins/` 目录
3. 重启服务器或使用插件管理器加载
4. 编辑 `plugins/InterConnect-Client-Spigot/config.yml` 配置文件
5. 再次重启或使用 `/ic reload` 重载配置

## 配置说明

```yaml
server:
  # InterConnect-Server 的 WebSocket 地址
  url: "ws://localhost:8000/ws"
  
  # 从 InterConnect-Server 获取的 UUID
  uuid: ""
  
  # 从 InterConnect-Server 获取的 Token
  token: ""
  
  # 服务器显示名称
  name: "MyServer"
  
  # 插件启动时自动连接
  auto-connect: true

events:
  # 启用/禁用各类事件的同步
  sync-player-join: true
  sync-player-quit: true
  sync-player-death: true
  sync-player-chat: true
  sync-player-message: true
```

## 指令列表

| 指令 | 权限 | 说明 |
|-----|------|------|
| `/ic reload` | `interconnect.admin` | 重载配置文件 |
| `/ic test` | `interconnect.admin` | 测试与 Server 的 API 连通性 |
| `/ic status` | `interconnect.admin` | 显示当前连接状态 |
| `/ic connect` | `interconnect.admin` | 手动连接到服务器 |
| `/ic disconnect` | `interconnect.admin` | 断开服务器连接 |
| `/ic version` | `interconnect.admin` | 显示版本信息 |
| `/ic help` | `interconnect.admin` | 显示帮助信息 |

## 获取 UUID 和 Token

### 方法一：使用 CLI（推荐）

在 InterConnect-Server 上执行：

```bash
npm run start add-node <服务器名称>
```

返回的 UUID 和 Token 即为配置所需的值。

### 方法二：使用 REST API

```bash
curl -X POST http://localhost:8000/manager/keys/<服务器名称> \
  -H "X-Admin-Token: <你的管理员密码的SHA256哈希>"
```

## 构建方法

```bash
# 克隆仓库
git clone https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot.git
cd InterConnect-Client-Spigot

# 使用 Maven 构建
mvn clean package

# 构建后的 jar 位于 target/ICC-Spigot-1.0.1.jar
```

## 依赖项

- [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) - WebSocket 客户端
- [org.json](https://github.com/stleary/JSON-java) - JSON 处理

## 开源协议

本项目采用 [GNU General Public License v3.0](LICENSE) 开源协议。

## 作者

**CloudWeave-YunZhi**

- GitHub: [@CloudWeave-YunZhi](https://github.com/CloudWeave-YunZhi)
- 项目地址: https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot

## 相关项目

- [InterConnect-Server](https://github.com/CloudWeave-YunZhi/InterConnect-Server) - WebSocket 中继服务器
