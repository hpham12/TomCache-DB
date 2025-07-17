package com.hpham.database.btree_disk.constants;

public class DataConstants {
  public static final int INT_SIZE_BYTES = 4;
  public static final int LONG_SIZE_BYTES = 8;
  public static final int POINTER_SIZE_BYTES = 8;
  public static final int CHAR_SIZE_BYTES = 1;
  public static final int STRING_SIZE_BYTES = 50;
  public static final int TYPE_SIGNAL_SIZE_BYTES = 1;
  public static final int INDEX_FILE_HEADER_SIZE_BYTES = 1;

  public static final int PAGE_SIZE_BYTES = 4 * 1024;
  // TODO: This is temporary, as boolean should take only 1 bit to save space
  public static final int BOOL_SIZE_BYTES = 1;

  public static final byte INT_TYPE_SIGNAL = 0x01;
  public static final byte STRING_TYPE_SIGNAL = 0x02;
  public static final byte LONG_TYPE_SIGNAL = 0x03;

}
