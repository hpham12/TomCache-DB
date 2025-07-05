package com.hpham.database.btree_disk.dataTypes;

import lombok.Builder;

import static com.hpham.database.btree_disk.constants.DataTypeSizes.INT_SIZE_BYTES;

@Builder
public class IntField extends SortableField<Integer> {
  private Integer value;
  @Override
  public byte[] serialize() {
    byte[] bytes = new byte[INT_SIZE_BYTES];
    for (int i = INT_SIZE_BYTES - 1; i >= 0; i--) {
      bytes[INT_SIZE_BYTES - 1 - i] = (byte) ((value >> (i * 8)));
    }

    return bytes;
  }

  public static Integer deserialize(byte[] bytes, int start) {
    int num = 0;
    for (int i = 0; i < INT_SIZE_BYTES; i++) {
      num = num | (bytes[start + i]);
      if (i < INT_SIZE_BYTES - 1) {
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
