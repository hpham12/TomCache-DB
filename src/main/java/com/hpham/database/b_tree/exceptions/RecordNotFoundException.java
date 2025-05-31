package com.hpham.database.b_tree.exceptions;

public class RecordNotFoundException extends Exception {
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
