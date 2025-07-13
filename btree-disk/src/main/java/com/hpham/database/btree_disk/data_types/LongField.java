package com.hpham.database.btree_disk.data_types;

import lombok.Builder;

import java.nio.ByteBuffer;

import static com.hpham.database.btree_disk.constants.DataConstants.LONG_SIZE_BYTES;

@Builder
public final class LongField extends SortableField<Long> {
  private Long value;

  @Override
  public ByteBuffer serialize() {
    ByteBuffer bb = ByteBuffer.allocateDirect(LONG_SIZE_BYTES);
    bb.putLong(value);
    bb.flip();

    return bb;
  }

  public static Long deserialize(ByteBuffer bb, int start) {
    return bb.getLong(start);
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
