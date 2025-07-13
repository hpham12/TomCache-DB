package com.hpham.database.btree_disk.file_formats.index;

import com.hpham.database.btree_disk.data_types.Serializable;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class IndexFileHeader implements Serializable {
  private Character keyType;
  private Long rootOffset;

  @Override
  public ByteBuffer serialize() {
    ByteBuffer bb = ByteBuffer.allocate(9);
    bb.putChar(keyType);
    bb.putLong(rootOffset);
    bb.flip();

    return bb;
  }
}
