package com.hpham.database.btree_disk.file_formats.record;

import com.hpham.database.btree_disk.data_types.Serializable;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class RecordFileHeader implements Serializable {
  private Integer recordSize;

  @Override
  public ByteBuffer serialize() {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.putInt(recordSize);
    bb.flip();
    return bb;
  }
}
