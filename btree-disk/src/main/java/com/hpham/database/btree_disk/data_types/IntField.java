package com.hpham.database.btree_disk.data_types;

import lombok.Builder;

import java.nio.ByteBuffer;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;

@Builder
public final class IntField extends SortableField<Integer> {
  private Integer value;

  @Override
  public ByteBuffer serialize() {
    ByteBuffer bb = ByteBuffer.allocateDirect(INT_SIZE_BYTES);
    bb.putInt(value);
    bb.flip();

    return bb;
  }

  public static Integer deserialize(ByteBuffer bb, int start) {
    return bb.getInt(start);
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

  @Override
  public Integer getSize() {
    return INT_SIZE_BYTES;
  }

  @Override
  public Byte getTypeSignal() {
    return INT_TYPE_SIGNAL;
  }
}
