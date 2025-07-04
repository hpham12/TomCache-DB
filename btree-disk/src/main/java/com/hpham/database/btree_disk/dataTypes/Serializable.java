package com.hpham.database.btree_disk.dataTypes;

public interface Serializable<T> {
  byte[] serialize();
}
