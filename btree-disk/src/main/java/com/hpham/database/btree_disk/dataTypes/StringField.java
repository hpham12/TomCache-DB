package com.hpham.database.btree_disk.dataTypes;

import lombok.Builder;

import static com.hpham.database.btree_disk.constants.DataTypeSizes.INT_SIZE_BYTES;

/**
 * Class representing a string field.
 * String has maximum length of {@code Integer.MAX_VALUE}.
 * */
@Builder
public class StringField extends SortableField<String> {
  String value;

  /**
   * String is deserialized in the following way:
   * <pre>
   *      String {
   *        byte[4]       length;
   *        byte[length]  content;
   *      }
   * </pre>
   * */
  @Override
  public byte[] serialize() {
    int stringLength = value.length();
    byte[] bytes = new byte[INT_SIZE_BYTES + stringLength];
    int currentByteIndex = 0;
    for (int i = INT_SIZE_BYTES - 1; i >= 0; i--) {
      bytes[currentByteIndex] = (byte) ((stringLength >> (i * 8)));
      currentByteIndex++;
    }

    for (int i = 0; i < stringLength; i++) {
      bytes[currentByteIndex] = (byte) value.charAt(i);
      currentByteIndex++;
    }

    return bytes;
  }

  /**
   * Static method to deserialize a sequence of bytes to {@code String}.
   *
   * @param bytes underlying bytes to deserialize.
   * @param start position the underlying byte array to start the deserialization.
   * */
  public static String deserialize(byte[] bytes, int start) {
    int stringLength = 0;
    for (int i = 0; i < INT_SIZE_BYTES; i++) {
      stringLength = stringLength | (bytes[start + i]);
      if (i < INT_SIZE_BYTES - 1) {
        stringLength = stringLength << 8;
      }
    }

    int stringContentStart = start + INT_SIZE_BYTES;

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < stringLength; i++) {
      sb.append((char) bytes[stringContentStart + i]);
    }

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
}
