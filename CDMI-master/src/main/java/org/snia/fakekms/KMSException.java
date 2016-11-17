package org.snia.fakekms;

/**
 * Exceptions thrown by the KMS are derived from this class
 */
public class KMSException extends Exception {
    public KMSException() {
        super();
    }

    public KMSException(String message) {
        super(message);
    }

    public KMSException(Throwable cause) {
        super(cause);
    }

    public KMSException(String message, Throwable cause) {
        super(message, cause);
    }
}

