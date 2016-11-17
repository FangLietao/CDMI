package org.snia.fakekms;

/**
 * Exceptions thrown by the KMS are derived from this class
 */
public class KMSKeyExistsException extends Exception {
    public KMSKeyExistsException() {
        super();
    }

    public KMSKeyExistsException(String message) {
        super(message);
    }

    public KMSKeyExistsException(Throwable cause) {
        super(cause);
    }

    public KMSKeyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
