package com.hpham.database.btree_disk.constants;

public class DataTypeSizes {
  public static final int INT_SIZE_BYTES = 4;
  public static final int POINTER_SIZE_BYTES = 8;
  public static final int CHAR_SIZE_BYTES = 1;
  // TODO: This is temporary, as boolean should take only 1 bit to save space
  public static final int BOOL_SIZE_BYTES = 1;
}
