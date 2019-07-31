package com.xavier.snmp.exception;

import com.xavier.snmp.exception.base.SnmpRuntimeException;

/**
 * InitializationException
 *
 * @author NewGr8Player
 */
public final class InitializationException extends SnmpRuntimeException {

    /**
     * @param message the detail message
     */
    public InitializationException(final String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the exception cause
     */
    public InitializationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
