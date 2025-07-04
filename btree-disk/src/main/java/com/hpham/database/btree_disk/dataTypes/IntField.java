package com.hpham.database.btree_disk.dataTypes;

import lombok.Builder;

@Builder
public class IntField extends SortableField<Integer> {
  private Integer value;
  @Override
  public byte[] serialize() {
    byte[] bytes = new byte[4];
    for (int i = 3; i >= 0; i--) {
      bytes[3 - i] = (byte) ((value >> (i * 8)));
    }

    return bytes;
  }

  public static Integer deserialize(byte[] bytes) {
    int num = 0;
    for (int i = 0; i < 4; i++) {
      num = num | (bytes[i]);
      if (i < 3) {
        num = num << 8;
      }
    }

    return num;
  }

  @Override
  public int compareTo(SortableField<Integer> o) {
    return this.value.compareTo(o.getValue());
  }

  @Override
  public Integer getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof IntField) {
      return this.value.equals(((IntField) o).value);
    }

    return false;
  }

  public static IntField fromValue(Integer value) {
    return IntField.builder().value(value).build();
  }
}
