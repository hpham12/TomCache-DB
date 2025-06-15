package com.hpham.database.btree.exceptions;

/**
 * {@code InvalidMethodInvocationException} is thrown when a method is called on a tree node
 * in which it is not supposed to.
 * */
public class InvalidMethodInvocationException extends RuntimeException {
    private final String message;

    @Override
    public String getMessage() {
        return String.format(message);
    }

    public InvalidMethodInvocationException(String message) {
        this.message = message;
    }
}
