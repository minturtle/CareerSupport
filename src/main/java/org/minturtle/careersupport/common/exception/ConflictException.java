package org.minturtle.careersupport.common.exception;

public class ConflictException extends RuntimeException{

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
