# Brobot Architecture Proposals

This directory contains proposed architectural designs and patterns that have been explored but not yet implemented (or decided against).

## Purpose

- **Preserve Design Thinking**: Documents architectural explorations and their rationale
- **Avoid Duplication**: Prevents future developers from repeating the same design exploration
- **Decision Reference**: Provides context for architectural decisions made (or not made)
- **Future Implementation**: Serves as starting point if proposals are revisited

## Status Definitions

| Status | Meaning |
|--------|---------|
| **PROPOSED** | Initial design proposal, not yet reviewed |
| **UNDER REVIEW** | Being evaluated for implementation |
| **APPROVED** | Approved for implementation, work not started |
| **REJECTED** | Decided against implementation |
| **SUPERSEDED** | Replaced by different approach |
| **NO TIMELINE** | Valid proposal but no scheduled implementation |

## Current Proposals

### Test Logging Architecture
- **File**: [test-logging-architecture.md](./test-logging-architecture.md)
- **Status**: NO TIMELINE - Not scheduled for implementation
- **Proposal Date**: 2024
- **Last Reviewed**: 2025-01-16
- **Summary**: Factory-based test logging system with TestLoggerFactory pattern
- **Decision**: Current test logging infrastructure (MockLoggerFactory) meets needs adequately

## Adding New Proposals

When adding a new architectural proposal:

1. **Create document** with clear "PROPOSED" status in frontmatter
2. **Include metadata**:
   ```yaml
   ---
   title: [Feature Name] Architecture (PROPOSED)
   status: PROPOSED
   proposal_date: YYYY-MM-DD
   author: [Your Name]
   ---
   ```
3. **Add to this README** with summary and status
4. **Link to related documentation** showing current implementation
5. **Explain rationale** for the proposal

## Moving from Proposals to Documentation

When a proposal is implemented:

1. Remove "PROPOSED" status from document
2. Move to appropriate documentation directory
3. Update code examples to reflect actual implementation
4. Update this README to mark as "IMPLEMENTED" with link to final location
5. Consider keeping proposal copy for historical reference

## Related Documentation

- [Contributing Guide](../../CONTRIBUTING.md) - How to contribute to Brobot
- [Architecture Overview](../docs/02-architecture/overview.md) - Current architecture
- [Testing Guide](../docs/04-testing/testing-intro.md) - Current testing patterns
