package com.hpham.database.btree_disk.dataTypes;

public abstract class Field<T> implements Serializable<T> {
  abstract T getValue();
}
