package com.hpham.database.btree_disk.data_types;

import java.nio.ByteBuffer;

public interface Serializable {
  ByteBuffer serialize();
}
