# 🚧 Development Roadmap: Queryable Relational Cache DB (TomCache-DB)

> A fast, embeddable SQL-based cache database with in-memory and disk persistence, source connectivity, and rich querying support.

---

## ✅ Core Requirements

1. Relational (SQL)
2. Queryable via SELECT
3. Caching mechanism with TTL/refresh
4. Works off in-memory and disk
5. Fast reads
6. Embeddable in any app
7. Supports transactions and concurrency
8. Connects to external data source (DB/API) for refresh

---

## 📦 Phase 0: Project Bootstrap

- [ ] Choose OSS license (MIT/Apache 2.0)
- [ ] Init Go module, folders: `engine/`, `sql/`, `storage/`, `source/`
- [ ] Set up GitHub repo, CI (GitHub Actions)
- [ ] Add `README.md` with project vision
- [ ] Optionally add CLI/REPL for manual testing

---

## 🧱 Phase 1: Core Storage Engine

- [ ] Define table schema, types, primary key
- [ ] Implement in-memory row store (map or slice)
- [ ] Add on-disk persistence (append-only JSON/log)
- [ ] Load persisted data at startup
- [ ] Add TTL support per row with expiry goroutine

---

## 🔍 Phase 2: SQL Query Engine (Read-Only)

- [ ] Implement basic SQL parser:
  - SELECT FROM WHERE
  - Operators: `=`, `<`, `>`, `AND`, `OR`, `LIMIT`
- [ ] Query executor: table scan + filter
- [ ] Define `QueryResult` output
- [ ] Respect TTL (exclude expired rows)

---

## ✍️ Phase 3: Mutations

- [ ] INSERT INTO ... VALUES (...)
- [ ] UPDATE ... WHERE ...
- [ ] DELETE ... WHERE ...
- [ ] Persist all writes to disk
- [ ] Track row metadata (created/updated/expired)

---

## 🔄 Phase 4: Source Connectivity (Refresh)

- [ ] Define `SourceConnector` interface
- [ ] Register a source per table (e.g., Postgres, API)
- [ ] Manual `db.Refresh("table")` support
- [ ] TTL-based auto-refresh (optional background thread)
- [ ] Implement Postgres + REST mock connector

---

## 🤝 Phase 5: Transactions & Concurrency

- [ ] Add `sync.RWMutex` protection
- [ ] Batch write + flush logic
- [ ] Simple transaction API:
  ```go
  tx := db.Begin()
  tx.Insert(...)
  tx.Commit()
  ```

---

## 🚀 Phase 6: Caching Features

- [ ] TTL support on row/table
- [ ] Memory cap + eviction strategy (LRU)
- [ ] Hit/miss counters + logging

---

## 🔗 Phase 7: Embeddable API

- [ ] Clean API: `Open()`, `Query()`, `Insert()`, `Refresh()`
- [ ] Prometheus metrics (optional)
- [ ] Easy to embed in Go apps

---

## 📚 Phase 8: Documentation & OSS Launch

- [ ] Examples for Go and Java usage
- [ ] Full user documentation
- [ ] Benchmarks and performance analysis
- [ ] Blog post + HN/Reddit launch
- [ ] Open to community feedback

---

## ✅ Roadmap Checklist

```markdown
- [ ] Phase 0: Bootstrap
- [ ] Phase 1: Storage Engine
- [ ] Phase 2: Query Engine
- [ ] Phase 3: Mutations
- [ ] Phase 4: Source Connectivity
- [ ] Phase 5: Transactions
- [ ] Phase 6: Caching
- [ ] Phase 7: Embedding
- [ ] Phase 8: Docs + Launch
```

---

## ✨ Future Possibilities

- Support for joins / subqueries
- Multi-table refresh + sync
- Delta-based persistence instead of full log
- WASM-compatible version
- TypeScript/Java/Python SDKs