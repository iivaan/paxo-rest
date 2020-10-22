package com.paxovision.rest.exception;

public class PaxoRestException extends RuntimeException{
    public PaxoRestException() {
    }

    public PaxoRestException(String message) {
        super(message);
    }

    public PaxoRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaxoRestException(Throwable cause) {
        super(cause);
    }

    public PaxoRestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
