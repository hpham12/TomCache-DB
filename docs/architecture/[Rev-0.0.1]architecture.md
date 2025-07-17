# High-level Architecture

![[Rev-0.0.1]High-level-architecture.png](../images/%5BRev-0.0.1%5DHigh-level-architecture.png)

## Notations:
- `SchemaType`: The java class type of the user-defined schema
- `KeyType`: The type of the key

## `Get(key)` flow

- Client library
```java
public SchemaType get(KeyType key) {
  return api.get(key);
}
```

- API layer:
```java
public Record get(KeyType key) {
  return toDomainObject(bTree.getByKey(key));
}

private toDomainObject(Class<?> to, Record record) {
  // convert the record to domain object
}
```

- B-Tree layer:
```java
public Record getByKey() {
  // traverse the tree to get the record offset, using IndexFile
  //....
  //....
  // Then use DataFile to get Record
  return SerializationUtil.serialize(dataFile.read(recordOffset), Record.class);
}
```

- IndexFile layer
```java
public byte[] read(long offset);
```

## `Insert(entry)` flow

- Client library
```java
public SchemaType insert(SchemaType entry) {
  return api.insert(entry);
}
```

- API layer:
```java
public Record insert(SchemaType entry) {
  return bTree.insert(toRecord(entry));
}

private Record toRecord(Object entry) {
  // convert from entry to record
}
```

- B-Tree layer:
```java
public Record insert(Record record) {
  // 1. Operate on B-tree to find the record metadata: parent, isRoot
  // 2. Update leaf node and/or parent pointer
  // 3. Might need to do update multiple times in case the insert is bubbled up
  var recordOffset = dataFile.insert(SerializationUtil.serialize(record));
  var indexOffset = dataFile.update(offset, SerializationUtil.serialize(recordToUpdate));
  //.....
}
```

- IndexFile layer
```java
public long insert(byte[] bytes); // returns offset
public long update(long offset, byte[] bytes); //returns offset
```
