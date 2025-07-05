package com.hpham.database.btree.exceptions;

/**
 * {@code RecordNotFoundException} is thrown when a record is not found in the B-Tree.
 */
public class RecordNotFoundException extends RuntimeException {
  private final Object key;

  @Override
  public String getMessage() {
    return String.format(
        "Record with key %s not found in B-Tree",
        key
    );
  }

  public RecordNotFoundException(Object key) {
    this.key = key;
  }
}
