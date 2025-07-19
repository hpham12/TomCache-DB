package com.hpham.database.btree_disk.data_types;

import lombok.Builder;

import java.nio.ByteBuffer;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.INT_TYPE_SIGNAL;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.STRING_TYPE_SIGNAL;

/**
 * Class representing a string field.
 * String has maximum length of {@code Integer.MAX_VALUE}.
 * */
@Builder
public final class StringField extends SortableField<String> {
  String value;

  /**
   * String is deserialized in the following way:
   * <pre>
   *      String {
   *        byte[4]       length;
   *        byte[length]  content;
   *      }
   * </pre>
   * Padding is added to make sure size is {@code STRING_SIZE_BYTES}
   * */
  @Override
  public ByteBuffer serialize() {
    int stringLength = value.length();
    assert stringLength < 46;

    byte[] bytes = new byte[50];
    int currentByteIndex = 0;
    for (int i = INT_SIZE_BYTES - 1; i >= 0; i--) {
      bytes[currentByteIndex] = (byte) ((stringLength >> (i * 8)));
      currentByteIndex++;
    }

    for (int i = 0; i < stringLength; i++) {
      bytes[currentByteIndex] = (byte) value.charAt(i);
      currentByteIndex++;
    }

    return ByteBuffer.wrap(bytes);
  }

  /**
   * Static method to deserialize a sequence of bytes to {@code String}.
   *
   * @param bb underlying bytes to deserialize.
   * @param start position the underlying byte array to start the deserialization.
   * */
  public static String deserialize(ByteBuffer bb, int start) {
    int stringLength = bb.getInt(start);

    StringBuilder sb = new StringBuilder();
    bb.position(start + INT_SIZE_BYTES);
    for (int i = 0; i < stringLength; i++) {
      sb.append((char) bb.get());
    }

    bb.position(start + STRING_SIZE_BYTES);

    return sb.toString();
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

  @Override
  public Integer getSize() {
    return STRING_SIZE_BYTES;
  }

  @Override
  public Byte getTypeSignal() {
    return STRING_TYPE_SIGNAL;
  }
}
