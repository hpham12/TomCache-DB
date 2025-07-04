package com.hpham.database.btree_disk.dataTypes;

public abstract class SortableField<T> extends Field<T> implements Comparable<SortableField<T>> {

  @SuppressWarnings("unchecked")
  public static <T> SortableField<T> fromValue(T value) {
    if (value instanceof Integer) {
      return (SortableField<T>) IntField.fromValue((Integer) value);
    }
    if (value instanceof String) {
      return (SortableField<T>) StringField.fromValue((String) value);
    }

    return null;
  }
}
