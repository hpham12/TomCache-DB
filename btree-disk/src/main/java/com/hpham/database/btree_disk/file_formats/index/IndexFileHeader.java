package com.hpham.database.btree_disk.file_formats.index;

import com.hpham.database.btree_disk.data_types.Serializable;
import lombok.Builder;
import lombok.Data;

import java.nio.ByteBuffer;

@Builder
public class IndexFileHeader implements Serializable {
  private byte keyType;
  private long rootOffset;

  @Override
  public ByteBuffer serialize() {
    ByteBuffer bb = ByteBuffer.allocate(9);
    bb.put(keyType);
    bb.putLong(rootOffset);
    bb.flip();

    return bb;
  }

  public static Integer size() {
    return 9;
  }
}
