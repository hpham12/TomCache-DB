package com.hpham.database.btree_disk.dataTypes;

public abstract class Field<T> implements Serializable<T> {
  @SuppressWarnings("unchecked")
  public static <T> Field<T> fromValue(T value) {
    if (value instanceof Integer) {
      return (Field<T>) IntField.fromValue((Integer) value);
    }
    if (value instanceof String) {
      return (Field<T>) StringField.fromValue((String) value);
    }

    return null;
  }

  abstract T getValue();
}
