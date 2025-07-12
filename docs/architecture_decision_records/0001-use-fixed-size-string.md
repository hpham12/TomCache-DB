# ADR-0001: Use Fixed-Size String

## Context
I want each index to have the same size to avoid slotted pages, offset tracking, etc.

## Decision
Use fixed-length strings key (64 bytes max) for all MVP index. Strings can still have variable length, but paddings will be added to make sure it reaches 64 bytes

## Consequences
+ Simplifies serialization and layout
- Truncates longer strings