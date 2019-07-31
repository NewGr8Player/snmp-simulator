package com.xavier.snmp.exception.base;

/**
 * SnmpRuntimeException
 *
 * @author NewGr8Player
 */
public class SnmpRuntimeException extends RuntimeException {
    /**
     * @param message the detail message
     */
    public SnmpRuntimeException(final String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the exception cause
     */
    public SnmpRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
