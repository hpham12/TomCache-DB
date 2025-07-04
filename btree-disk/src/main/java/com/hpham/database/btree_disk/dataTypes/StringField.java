package com.hpham.database.btree_disk.dataTypes;

import lombok.Builder;

// TODO: Implement serialize and deserialize method
@Builder
public class StringField extends SortableField<String> {
  String value;

  @Override
  public byte[] serialize() {
    return null;
  }

  @Override
  String getValue() {
    return value;
  }

  @Override
  public int compareTo(SortableField<String> o) {
    return this.value.compareTo(o.getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof StringField) {
      return this.value.equals(((StringField) o).value);
    }

    return false;
  }

  public static StringField fromValue(String value) {
    return StringField.builder().value(value).build();
  }
}
