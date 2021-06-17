package org.eclipse.sparkplug.tck.message.exception;

import com.hivemq.extension.sdk.api.annotations.NotNull;

public class UnknownMessageTypeException extends RuntimeException {
    public UnknownMessageTypeException() {
    }

    public UnknownMessageTypeException(final @NotNull String message) {
        super(message);
    }

    public UnknownMessageTypeException(final @NotNull String message, final @NotNull Throwable cause) {
        super(message, cause);
    }

    public UnknownMessageTypeException(final @NotNull Throwable cause) {
        super(cause);
    }

    public UnknownMessageTypeException(final @NotNull String message,
                                       final @NotNull Throwable cause,
                                       final boolean enableSuppression,
                                       final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
