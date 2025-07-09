---
sidebar_position: 1
---

# Integrations Overview

Brobot can be integrated with various external tools and services to extend its automation capabilities beyond traditional GUI automation.

## Available Integrations

### MCP Server (Model Context Protocol)

Enable AI agents to control Brobot automations through a RESTful API. This integration allows Large Language Models (LLMs) like GPT-4, Claude, and others to:

- Observe application states with screenshots
- Execute GUI automation actions
- Make intelligent decisions based on visual feedback

[Learn more about MCP Server →](./mcp-server/getting-started)

### Future Integrations

We're working on additional integrations to expand Brobot's ecosystem:

- **IDE Plugins**: Visual Studio Code and IntelliJ IDEA extensions
- **CI/CD Tools**: Jenkins, GitHub Actions, and GitLab CI integration
- **RPA Platforms**: Connect with UiPath, Automation Anywhere, and Blue Prism
- **Monitoring Tools**: Integration with Datadog, New Relic, and Prometheus
- **Cloud Services**: AWS, Azure, and Google Cloud automation

## Integration Architecture

All Brobot integrations follow a consistent pattern:

```
External Tool → Integration Layer → Brobot Core → Target Application
```

This architecture ensures:
- **Consistency**: All integrations use the same Brobot state management
- **Reliability**: Brobot's proven automation engine handles all actions
- **Flexibility**: New integrations can be added without modifying core
- **Maintainability**: Clear separation of concerns

## Getting Started

Choose an integration based on your use case:

- **AI-Driven Automation**: Use the [MCP Server](./mcp-server/getting-started)
- **Development Workflow**: Wait for upcoming IDE plugins
- **Enterprise RPA**: Contact us for custom integration support

## Contributing

Want to build a new integration? We welcome contributions! Check our [Integration Development Guide](https://github.com/jspinak/brobot/blob/main/CONTRIBUTING.md) for details on creating new integrations.