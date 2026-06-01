package com.teragrep.rlo_06.refactored;

public final class ClaimFailedException extends RuntimeException {
    public ClaimFailedException(final Class<?> clazz, final String message) {
        super("Claim " + clazz.getSimpleName() + " failed: " + message);
    }

    public ClaimFailedException(final Class<?> clazz, final String message, final Throwable cause) {
        super("Claim " + clazz.getSimpleName() + " failed: " + message, cause);
    }
}
