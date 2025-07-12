# ADR-0002: Use Fixed-Size Index and Record (For MVP)

## Context
I want each index and record to have the same size to avoid slotted pages, offset tracking, etc.

## Decision
Use fixed-length index and record.
- Index has a fixed-size of 4KB
- The first value added to the record file will set the fixed record size

## Consequences
+ Simplifies serialization and layout
- Schema can only support primitive types (fine for MVP)