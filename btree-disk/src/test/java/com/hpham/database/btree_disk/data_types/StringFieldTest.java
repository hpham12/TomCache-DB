package com.hpham.database.btree_disk.data_types;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static com.hpham.database.btree_disk.constants.DataConstants.INT_SIZE_BYTES;
import static com.hpham.database.btree_disk.constants.DataConstants.LONG_SIZE_BYTES;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for {@link StringField}.
 */
public class StringFieldTest {
  @Test
  void testSerialization() {
    String testString = "HelloWorld";
    StringField field = StringField.fromValue(testString);
    ByteBuffer serialized = field.serialize();
    String deserialized = StringField.deserialize(serialized, 0);

    assertThat(deserialized).isEqualTo(testString);
  }

  @Test
  void testSerializationWithNonZeroStart() {
    String testString = "HelloWorld";
    StringField field = StringField.fromValue(testString);
    ByteBuffer serialized = field.serialize();

    ByteBuffer buffer = ByteBuffer.allocateDirect(100);
    buffer.putInt(2);
    buffer.putLong(2132L);
    buffer.put(serialized);

    String deserialized = StringField.deserialize(buffer, INT_SIZE_BYTES + LONG_SIZE_BYTES);

    assertThat(deserialized).isEqualTo(testString);
  }
}
