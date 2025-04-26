# Database Internals

## Chapter 1: Intro and Overview

### DBMS Architecture:
- Every DB is built slightly different, but there are some common themes.
- DBMS is a client/server model, where DBMS instance (node) is the server, and application instance is client.

![](DBMS-architecture.png)

- Transport Subsystem:
	+ Receives client requests. Requests come in the form of queries, most often expressed in some query language.
	+ Also responsible for comminucation with other nodes in the db cluster

> Upon receipt, the transport subystem hands the query over to a query processer

- Query Processor:
	+ Query parser: Parses interprets, and validates the query. Later, access control checks are performed, as they can be done fully only after the query is interpreted

	+ Query optimizer: The parsed query is passed to the query optimizer. The query is usually presented in the form of an execution plan (query plan): a sequence of ops that have to be carried out for its results to be considered complete. Since the same query can be satisfied using different execution plans that can vary in efficiency, the optimizer picks the best available plan

> The execution plan is handled by the execution engine

- Execution engine:
	+ the execution plan is handled by the execution engine, which collects the results of execution of local and remote operations

> For local queries, execution engine ask storage engine to retrieve the data

- Storage Engine:
	+ Transaction manager: Schedules transactions and ensures they cannot leave the database in a logically inconsistent state

	+ Lock manager: Locks on the database objects for the running transactions, ensuring that concurrent operations do not violate physical data integrity

	+ Access methods (storage structure): These manage access and organizing data on disk. Access methods include heap files and storage structures such as B-Trees

	+ Buffer manager: This manager caches data pages in memory

	+ Recovery manager: Maintains the operation log and restoring the system state in case of a failure

	+ Together, transaction manager and lock manager are responsible for concurrency control

### Memory vs Disk-Based DBMS

- In-memory dbms:
	- Maintain backups on disk to provide durability and prevent loss of the volatile data
	- Before the operation can be considered complete, its results have to be written to a sequential log file
	- In-memory stores maintain a backup copy and periodically update it asynchronously. During recovery, database contents can be restored from the backup and logs
	- Log records are usually applied to backup in batches. After the batch of log records is processed, backup holds a database snapshot for a specific point in time, and log contents up to this point can be discarded. This process is called CHECKPOINTING
	
- Disk-based DMS:
	- Specialized storage structure, optimized for disk access
	- Disk-based storage structures often have a form of wide and short trees

### Column vs Row-Oriented DBMS

- One of the ways to classify databases is by how the data is stored on disk: row- or column-wise

- Tables can be partitioned either horizontally (storing values belonging to the same row together), or vertically (storing values belonging to the same column together).

![](Row-vs-column.png)

- Row-oriented DBMS:
	- Stores data in records or rows
	- Useful in scenarios where we access data by row, storing entire rows together improves spatial locality (fields on same row are adjacent)
	- Expensive when accessing individual fields of multiple user records

- Column-oriented DMS:
	- Stored data vertically
	- Good for queries by column

### Data Files and Index Files

- Database systems do use files for storing the data, but instead of relying on filesystem hierarchies of directories and files for locating records, they compose files using implementation-specific formats. The main reasons to use specialized file organization over flat files are:
	+ Storage efficiency: Files are organized in a way that minimizes storage overhead per stored data record
	+ Access efficiency: Records can be located in the smallest possible number of steps

- DBMS store data records, consisting of multiple fields, in tables, where each table is usually representated as a separate file

- To locate a record, DBMS uses indexes: auxiliary data structures that allow it to efficiently locate data records without scanning an entire table on every access. Indexes are built using a subset of fields identifying the record

- DBMS usually separates data files and index files. Filesare partitioned into pages, which typically have the size of a single or multiple disk blocks. Pages can be organized as sequences of records or as a slotted pages

- New records (insertions) and updates to the exisiting records are represented by key/value pairs. Most modern storage systems do not delete data from pages explicitly. Instead, they use deletion markers (also called tombstones), which contain deletion metadata, such as a key and a timestamp. Space occupied by the records shadowed by their updates or deletion markers is reclaimed during garbage collection, which reads the pages, writes the live (i.e., nonshadowed) records to the new place, and discards the shadowed ones

### Data Files

- Data files (also called primary files), can be implemented as index-organized tables (IOT), heap-organized tables (heap files), or hash-organized tables (hashed files).

- Heap files:
	+ Records are not required to follow any particular order --> No additional work or file re-organization is required when new pages are appended.
	+ Requires additional index structures, pointing to the locations where data records are stored, to make them searchable

- Hashed files:
	+ Records are stored in buckets
	+ Hash value of key determines which bucket a record belongs to

- Index-organized tables (IOTs):
	+ Stores data records in the index itself --> Reduces the number of disk seek

### Index Files

- An index is a structure that organizes data records on disk in a way that facilitates efficient retrieval operations.

- Index files are organized as specilized structure that map keys to locations in data files where the records identified by these keys (in case of heap files) or primary keys (in case of index-organized tables) are stored

- An index on a primary file is called a primary index
	+ In most cases, primary index is built over a primary key or a set of keys identified as primary. All other indexes are secondary

- Secondary index:
	+ Can point directly to the data record, or simply store it primary key
	+ Multiple secondary indexes can point to the same record
	+ Secondary indexes may hold several entries per search key

- Clustered index:
	+ Order of data records follows the search key order
	+ Data records are usually stored in the same file of in a clustered file

- Non-clustered index:
	- Data stored in a separate file, and its order does not follow the key order

### Primary Key As An Indirection

- Referencing data directly:
	+ Reduce # of disk seeks
	+ Pay the extra cost of updating the pointers whenever the record is updated || relocated during a maintenance process

- Indirection through primary index:
	+ Reduce cost of pointers update
	+ Higher cost on read path

![](Index-Indirection.png)

- It is also possible to use a hybrid approach and store both data file offsets and primary keys. First, you check if the data offset is still valid and pay the extra cost of going through the primary key index if it has changed, updating the index file after finding a new offset.

### Buffering, Immutability, and Ordering

- A storage engine is based on some data structure. However, these structures do not describe the semantics of caching, recovery, transactionality, and other things that storage engines add on top of them.

- Storage structures of 3 common variables: they use Buffering (or avoid using it), use immutable (or mutable) files, and store values in order (or out of order)

- Buffering:
	+ Defines whether or not the storage structure chooses to collect a certain amount of data in memory before putting it to disk

- Mutability:
	+ Defines whether or not the storage structure reads parts of of the file, updates them, and writes the updated results at the same location in the file
	+ Immutable structures are append-only: once written, file contents are ot modified.

- Ordering:
	+ Defines whether or not the data records are stored in the key order in the pages on disk. In other words, the keys that sort closely are stored in contiguous segments on disk
	+ Ordering often defines whether or not we can efficiently scan the range of records, not only locate the individual data records

## Chapter 2: B-Tree Basics

- One of the most popular storage structure is a B-Tree

- Why BST is not a good alternative as a disk datastructure?
	+ Low fanout (fanout is the number of maximum allowed number of children per node), which reduce spatial locality
	+ Low fanout -> Increased tree height -> More disk seeks during traveral

- A version of the tree that would be better suited for dusk implementation has to exhibit the following props:
	+ High fanout to improve locality of the neighboring keys
	+ Low height to reduce the number of seeks during traversal

### B-Tree

- Smallest unit of disk data retrival is block, which is usually 512 bytes.

- Imagine data storage:
	+ Each entry in the table is 128B
	+ There are 1000 entries in total
	+ There is no index

	-> In the worst case, querying 1 entry can require up to 250 disk accesses
	-> Bad performance

	+ If there is a single level index (each index has a pointer to one entry stored on disk), and each index entry is 16 bytes
	+ In one read, we can each 32 index entries

	-> We only need at most (1000/32) + 1 = 33 disk accesses
	-> This is better, but we can still improve my setting a second level index

	+ Say we set up the second level index, which points to a block worth of first level index (32 entries), then we will have 32 second level indexes (each is 16B) -> 1 disk read can read the all second level indexes
	+ To find an entry, we need at most 1 + 1 + 1 = 3 disk accesses

	-> This is better

	-> With increasing number of entries, the number of index levels will increase

	-> We need a way - an algorithm - to do this index level creation automatically

	-> B-Tree is the data structure that allows us to do that

- B-Tree is an multiway search tree with some additional properties:
	+ Balanced: All leaf nodes are at the same level, ensuring consustent access time for any key
	+ Variable Node Size: Keys within each node are sorted in ascending order, enabling efficient searching and sequential access
	+ Sorted keys: Keys within each node are sorted in ascending order, enabling efficient searching and sequential access
	+ Minimum and Maximum Keys: Each node (except the root) as a minimum and maximum number of keys, ensuring balance and preventing extreme scenarios
	+ Minimum Degress (m): A B-Tree of order m has nodes with a maximum of m children
	+ Internal Nodes: Internal nodes (except the root) have between `ceil(m/2)` and m children
	+ Root Node: The root node can be a leaf or an internal node with 2 to m children
	+ Leaf Node: Leaf nodes do not have children and are all at the same level

- B-Tree vs B+-Tree:
	- B+-Tree: 
		+ Only leaf nodes have record pointers
		+ Every key will have its copy in the leaf node
		+ Leaf nodes are connected like a linked list

- To create a disk-based implementatiion, we need to go into details of how to layout B-Tree nodes on disk and compose on-disl layout using data-encoding formats

## Chapter 3: File Formats
t

