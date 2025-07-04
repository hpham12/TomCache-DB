package com.hpham.database.btree_disk.dataTypes;

import com.hpham.database.btree_disk.dataTypes.IntField;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntFieldTest {
  @Test
  void testSerialization() {
    IntField field = IntField.fromValue(2842);

    byte[] serialized = field.serialize();

    assertThat(field.deserialize(serialized)).isEqualTo(2842);
  }
}
