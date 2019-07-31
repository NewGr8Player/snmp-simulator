package com.xavier.snmp.exception.base;

/**
 * SnmpException
 *
 * @author NewGr8Player
 */
public class SnmpException extends Exception {
    /**
     * @param message the detail message
     */
    public SnmpException(final String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the exception cause
     */
    public SnmpException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
