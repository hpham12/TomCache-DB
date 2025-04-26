# 🚀 MVP Roadmap for Queryable Relational Cache DB (TomCache-DB)

### Goal:
To create a **lightweight, embeddable, relational database** with the ability to **cache**, **query** via SQL, and provide **disk persistence** in a fast and simple manner. The core features should focus on caching with basic relational capabilities for querying and data refresh.

---

### ✅ Core Features for MVP:

1. **Relational (SQL) support**  
   - A simple table structure with basic `SELECT`, `INSERT`, `UPDATE`, `DELETE` support.
   - Support only basic data types (int, string, etc.).

2. **Queryable (SQL-like interface)**  
   - Implement a basic SQL-like query engine for `SELECT` queries.
   - Support `WHERE` clauses, `=`, `<`, `>`, `AND`, and `LIMIT` operators.
   
3. **In-memory and disk persistence**  
   - Store data in-memory for fast access.
   - Persist data on disk to ensure durability after restarts (e.g., using append-only logs or simple file storage).
   
4. **Caching with TTL**  
   - Set TTL (Time-To-Live) for cached rows.
   - Remove expired data automatically.
   
5. **Basic Data Mutations (Write Operations)**  
   - Support simple `INSERT`, `UPDATE`, and `DELETE` operations.
   - Write operations should persist data to disk.
   
6. **Basic API**  
   - Provide a simple API for embedding the database into any Go application.
   - Expose functions like `Open()`, `Query()`, `Insert()`, and `Refresh()`.

---

### 🛠 Development Phases for MVP

#### Phase 1: **Setup and Basic Storage Engine** (2 Weeks)
- **Set up the project structure**:
  - Go modules, folders (`engine/`, `sql/`, `storage/`).
  - Basic GitHub repo setup and CI pipeline.
  - Choose an appropriate OSS license (MIT/Apache 2.0).
  
- **In-memory storage**:
  - Implement a simple in-memory row store (e.g., map-based storage).
  - Define table structure (columns, rows, primary keys).

- **Disk persistence**:
  - Store data to disk (e.g., JSON files, or append-only log).
  - Load persisted data at startup.
  - Simple append-only file for writing changes.

#### Phase 2: **Basic Query Engine** (2 Weeks)
- **SQL Parser**:
  - Implement a basic SQL-like parser that supports:
    - `SELECT`, `FROM`, `WHERE`.
    - Simple operators (`=`, `<`, `>`, `AND`, `OR`, `LIMIT`).

- **Query Execution**:
  - Implement a query executor that can scan the in-memory store and apply filters.
  - Return results based on the query parameters.

- **TTL Support**:
  - Ensure expired rows are automatically filtered out during query execution.
  - Implement simple TTL-based row expiration.

#### Phase 3: **Data Mutations** (2 Weeks)
- **Basic CRUD Operations**:
  - `INSERT INTO ... VALUES (...)`: Insert data into tables.
  - `UPDATE ... SET ... WHERE ...`: Update existing rows based on criteria.
  - `DELETE FROM ... WHERE ...`: Delete rows based on criteria.
  
- **Persistence**:
  - Ensure that all changes (insert, update, delete) are written to disk.
  - Store metadata (like creation time, update time) for each row.

#### Phase 4: **Basic Caching Mechanism** (1 Week)
- **TTL-based Eviction**:
  - Implement the logic to automatically remove expired data (rows) from memory.
  - Implement TTL expiry checks when querying.
  
- **Eviction Strategy**:
  - For the MVP, use a simple TTL eviction without complex strategies like LRU (Least Recently Used).

#### Phase 5: **API and Embedding** (2 Weeks)
- **Simple API**:
  - Provide basic API functions to interact with the cache DB:
    - `Open()` to initialize the cache.
    - `Query()` to execute SQL-like queries.
    - `Insert()` to insert data into the cache.
    - `Refresh()` to manually trigger a refresh or data reloading from disk.
    
- **Embeddable in Applications**:
  - Ensure that it can be embedded in other Go applications seamlessly.

#### Phase 6: **Documentation and First Release** (1 Week)
- **README**:
  - Write a concise and clear `README.md` describing the purpose and usage of the cache DB.
  
- **Basic Documentation**:
  - Document the core features and API functions with simple examples.

- **Release**:
  - Publish the first stable version (v0.1.0) on GitHub.

---

### 🎯 MVP Milestones

1. **Storage Engine Setup**: Implement in-memory and disk persistence for basic data storage.
2. **Basic SQL Query Engine**: Support for SELECT queries with basic filtering and TTL.
3. **Write Operations**: Support INSERT, UPDATE, DELETE with persistence.
4. **Basic Caching**: Implement TTL eviction and ensure data is refreshed or expired correctly.
5. **Embeddable API**: Ensure a simple Go API that allows easy integration into other projects.
6. **First Public Release**: Package the MVP and prepare for public use with documentation.

---

### ✅ Post-MVP Considerations

Once the MVP is completed, you can think about adding:

- **Advanced SQL Features**: Joins, subqueries, and more complex query parsing.
- **Concurrency and Transactions**: Full ACID transaction support with isolation levels.
- **External Source Refresh**: Mechanism to pull in new data from external sources (DB, APIs).
- **Eviction Strategies**: More advanced memory management strategies like LRU or LFU.