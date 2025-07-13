package com.hpham.database.btree_disk.data_types;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for {@link IntField}
 * */
public class IntFieldTest {
  @Test
  void testSerialization() {
    IntField field = IntField.fromValue(2842);

    ByteBuffer serialized = field.serialize();

    assertThat(IntField.deserialize(serialized, 0)).isEqualTo(2842);
  }
}
