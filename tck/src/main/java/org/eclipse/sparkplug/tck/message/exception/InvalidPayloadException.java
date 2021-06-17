package org.eclipse.sparkplug.tck.message.exception;

import com.hivemq.extension.sdk.api.annotations.NotNull;

public class InvalidPayloadException extends RuntimeException {

    public InvalidPayloadException() {
    }

    public InvalidPayloadException(final @NotNull String message) {
        super(message);
    }

    public InvalidPayloadException(final @NotNull String message, final @NotNull Throwable cause) {
        super(message, cause);
    }

    public InvalidPayloadException(final @NotNull Throwable cause) {
        super(cause);
    }

    public InvalidPayloadException(final @NotNull String message,
                                   final @NotNull Throwable cause,
                                   final boolean enableSuppression,
                                   final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
