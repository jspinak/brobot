---
sidebar_position: 1
---

# Integrations Overview

Brobot can be integrated with various external tools and services to extend its automation capabilities beyond traditional GUI automation.

## Available Integrations

### MCP Server (Model Context Protocol)

:::danger EXPERIMENTAL FEATURE - USE WITH CAUTION

**The MCP Server is an experimental, untested proof-of-concept.** It is provided for research, prototyping, and exploration purposes only.

**DO NOT use in production environments.**

**Key Warnings**:
- No comprehensive testing has been performed
- APIs and interfaces may change without notice
- Security vulnerabilities may exist
- Performance and stability not guaranteed
- Limited error handling and recovery mechanisms
- Documentation may not reflect current implementation

**By using this feature, you acknowledge these risks and agree to use it for experimental purposes only.**

:::

Enable AI agents to control Brobot automations through a RESTful API. This integration allows Large Language Models (LLMs) like GPT-4, Claude, and others to:

- Observe application states with screenshots
- Execute GUI automation actions
- Make intelligent decisions based on visual feedback

[Learn more about MCP Server →](./mcp-server/getting-started)

:::info
The MCP Server is an **experimental proof-of-concept**. For production automation, please use the core Brobot framework directly with the standard API.
:::

### Potential Future Integrations

The Brobot ecosystem could be extended with additional integrations. Possible areas include:

- **IDE Plugins**: Visual Studio Code and IntelliJ IDEA extensions for development workflow
- **CI/CD Tools**: Jenkins, GitHub Actions, and GitLab CI pipeline integration
- **RPA Platforms**: Connectors for UiPath, Automation Anywhere, and Blue Prism
- **Monitoring Tools**: Observability with Datadog, New Relic, and Prometheus
- **Cloud Services**: AWS, Azure, and Google Cloud automation capabilities

:::info NOT YET IN DEVELOPMENT
These integrations do not currently exist and are not in active development. If you're interested in building integrations, we welcome contributions! Open a GitHub discussion to explore possibilities.
:::

## Proposed Integration Architecture

Future Brobot integrations will follow a consistent architectural pattern:

```
External Tool → Integration Layer → Brobot Core → Target Application
```

This design pattern will ensure:
- **Consistency**: All integrations use the same Brobot state management system
- **Reliability**: Brobot's well-tested automation engine (5,600+ tests, 95%+ pass rate) handles all actions
- **Flexibility**: New integrations can be added without modifying core functionality
- **Maintainability**: Clear separation of concerns between integration and core layers

:::tip CORE CONCEPTS
To build integrations, you'll need to understand Brobot's core concepts:
- [States](../01-getting-started/states.md) and [Transitions](../01-getting-started/transitions.md) - How Brobot models GUI applications
- [Core Concepts](../01-getting-started/core-concepts.md) - Overview of Brobot's architecture
- [Introduction](../01-getting-started/introduction.md) - Why Brobot exists
:::

## Getting Started

### Choose Your Approach

**For Experimentation & Research**:
- Try the [MCP Server](./mcp-server/getting-started) (experimental, AI-driven automation)
- Perfect for prototyping AI agents that control GUI applications
- Not suitable for production use

**For Production Automation**:
- Use the core Brobot framework directly with Java/Spring
- Start with the [Introduction](../01-getting-started/introduction.md) and [Core Concepts](../01-getting-started/core-concepts.md)
- See [Testing Documentation](../04-testing/testing-intro.md) for production-ready patterns
- Fully tested and production-ready

**For Building Custom Integrations**:
- Review the [proposed architecture](#proposed-integration-architecture) above
- Understand [States](../01-getting-started/states.md) and [Transitions](../01-getting-started/transitions.md)
- Open a [GitHub discussion](https://github.com/jspinak/brobot/discussions) to collaborate

## Contributing

Want to build a new integration? We welcome contributions!

**To contribute an integration**:
1. Review the [proposed architecture](#proposed-integration-architecture) above
2. Open a [GitHub discussion](https://github.com/jspinak/brobot/discussions) to discuss your integration idea
3. See the general [Contributing Guide](https://github.com/jspinak/brobot/blob/main/CONTRIBUTING.md) for code standards

**Help us improve the MCP Server!** If you're interested in AI-driven automation, we especially welcome:
- Bug reports and testing feedback
- API improvements and suggestions
- Documentation enhancements
- Example use cases and integrations

## Related Documentation

- **[Introduction](../01-getting-started/introduction.md)** - Why Brobot exists and core philosophy
- **[Core Concepts](../01-getting-started/core-concepts.md)** - Understanding Brobot's architecture
- **[States](../01-getting-started/states.md)** and **[Transitions](../01-getting-started/transitions.md)** - Model-based automation fundamentals
- **[Testing Documentation](../04-testing/testing-intro.md)** - Production-ready testing patterns
- **[MCP Server Getting Started](./mcp-server/getting-started.md)** - Experimental AI integration
