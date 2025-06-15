package com.hpham.database.btree.exceptions;

public class RecordAlreadyExistException extends RuntimeException {
    private final Object key;

    @Override
    public String getMessage() {
        return String.format(
                "Record with key %s is already present in B-Tree",
                key
        );
    }

    public RecordAlreadyExistException(Object key) {
        this.key = key;
    }
}
