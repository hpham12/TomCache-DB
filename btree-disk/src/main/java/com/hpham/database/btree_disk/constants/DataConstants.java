package com.hpham.database.btree_disk.constants;

public class DataConstants {
  public static final int INT_SIZE_BYTES = 4;
  public static final int POINTER_SIZE_BYTES = 8;
  public static final int CHAR_SIZE_BYTES = 1;
  public static final char STRING_SIZE_BYTES = 50;
  public static final char TYPE_SIGNAL_SIZE_BYTES = 1;
  // TODO: This is temporary, as boolean should take only 1 bit to save space
  public static final int BOOL_SIZE_BYTES = 1;

  public static final int INT_TYPE_SIGNAL = 1;
  public static final int STRING_TYPE_SIGNAL = 2;

  // TODO: Only for MVP. This should not be constant
}
