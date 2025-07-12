package com.hpham.database.btree_disk.data_types;

import lombok.Builder;

import static com.hpham.database.btree_disk.constants.DataConstants.LONG_SIZE_BYTES;

@Builder
public final class LongField extends SortableField<Long> {
  private Long value;

  @Override
  public byte[] serialize() {
    byte[] bytes = new byte[LONG_SIZE_BYTES];
    for (int i = LONG_SIZE_BYTES - 1; i >= 0; i--) {
      bytes[LONG_SIZE_BYTES - 1 - i] = (byte) ((value >> (i * 8)));
    }

    return bytes;
  }

  public static Long deserialize(byte[] bytes, int start) {
    long num = 0;
    for (int i = 0; i < LONG_SIZE_BYTES; i++) {
      num = num | (bytes[start + i]);
      if (i < LONG_SIZE_BYTES - 1) {
        num = num << 8;
      }
    }

    return num;
  }

  @Override
  public int compareTo(SortableField<Long> o) {
    return this.value.compareTo(o.getValue());
  }

  @Override
  public Long getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof LongField) {
      return this.value.equals(((LongField) o).value);
    }

    return false;
  }

  public static LongField fromValue(Long value) {
    return LongField.builder().value(value).build();
  }

  @Override
  public int getSize() {
    return LONG_SIZE_BYTES;
  }
}
