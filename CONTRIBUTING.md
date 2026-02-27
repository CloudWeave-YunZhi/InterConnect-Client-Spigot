# Contributing to InterConnect-Client-Spigot

首先，感谢您对 InterConnect-Client-Spigot 项目的关注！我们欢迎并感激所有形式的贡献。

## 如何贡献

### 报告 Bug

如果您发现了 Bug，请通过 [GitHub Issues](../../issues) 提交，并包含以下信息：

- 问题的清晰描述
- 复现步骤
- 期望行为与实际行为
- 服务器环境信息（Spigot版本、Java版本、插件版本等）
- 相关的错误日志

### 提出新功能

如果您有新功能的想法，欢迎通过 [GitHub Issues](../../issues) 提交功能请求：

- 清晰描述功能需求
- 解释为什么这个功能对项目有用
- 如果可能，提供实现思路

### 提交代码

1. **Fork 仓库**
   ```bash
   git clone https://github.com/CloudWeave-YunZhi/InterConnect-Client-Spigot.git
   cd InterConnect-Client-Spigot
   ```

2. **创建分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/bug-description
   ```

3. **编写代码**
   - 遵循现有的代码风格
   - 编写清晰的注释
   - 确保代码通过所有测试

4. **提交更改**
   ```bash
   git add .
   git commit -m "feat: add new feature"  # 使用约定式提交格式
   ```

5. **推送到您的 Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **创建 Pull Request**

## 代码规范

### 提交信息格式

我们使用 [约定式提交](https://www.conventionalcommits.org/) 格式：

- `feat:` 新功能
- `fix:` Bug 修复
- `docs:` 文档更新
- `style:` 代码格式（不影响代码运行的变动）
- `refactor:` 重构
- `test:` 测试相关
- `chore:` 构建过程或辅助工具的变动

示例：
```
feat: add reconnect delay configuration

Add configurable delay between reconnection attempts
instead of using fixed 5-second interval.

Closes #123
```

### Java 代码规范

- 使用 4 个空格缩进
- 类名使用大驼峰命名法（UpperCamelCase）
- 方法名和变量名使用小驼峰命名法（lowerCamelCase）
- 常量使用全大写和下划线（UPPER_SNAKE_CASE）
- 添加适当的 JavaDoc 注释

### 示例

```java
/**
 * Manages WebSocket connection to InterConnect-Server.
 * Handles connection lifecycle, message routing, and reconnection.
 */
public class WebSocketManager {
    
    private static final int DEFAULT_TIMEOUT = 5000;
    private boolean connected;
    
    /**
     * Establishes connection to the WebSocket server.
     * 
     * @return true if connection was successful
     */
    public boolean connect() {
        // implementation
    }
}
```

## 开发环境设置

### 要求

- JDK 8 或更高版本
- Maven 3.6+
- Git

### 构建

```bash
# 编译
mvn compile

# 运行测试
mvn test

# 打包
mvn package

# 安装到本地仓库
mvn install
```

## 测试

- 为新功能编写单元测试
- 确保所有测试通过后再提交 PR
- 测试覆盖率不应降低

## 文档

- 更新 README.md 如果更改了用户可见的功能
- 更新配置文件示例
- 为 API 更改添加 JavaDoc

## 行为准则

- 保持友好和尊重
- 欢迎新手，耐心解答问题
- 专注于对项目最有利的事情
- 尊重不同的观点和经验

## 获取帮助

如果您在贡献过程中需要帮助：

- 查看 [文档](docs/)
- 在 Issue 中提问
- 联系维护者

再次感谢您的贡献！
