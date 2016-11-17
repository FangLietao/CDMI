package com.philips.fakekms;

/**
 * Exceptions thrown by the KMS are derived from this class
 */
public class KMSKeyErrorException extends Exception {
    public KMSKeyErrorException() {
        super();
    }

    public KMSKeyErrorException(String message) {
        super(message);
    }

    public KMSKeyErrorException(Throwable cause) {
        super(cause);
    }

    public KMSKeyErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
