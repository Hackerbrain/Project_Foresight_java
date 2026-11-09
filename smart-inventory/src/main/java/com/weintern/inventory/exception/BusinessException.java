package com.weintern.inventory.exception;

/** A rule of the business was violated (e.g. insufficient stock). The message is safe to show to users. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
