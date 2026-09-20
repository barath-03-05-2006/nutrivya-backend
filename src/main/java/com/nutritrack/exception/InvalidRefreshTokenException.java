package com.nutritrack.exception;

/**
 * Thrown when a refresh token is missing, unknown, or expired.
 * The client should treat this as "session over" and send the user to login,
 * as opposed to an expired *access* token, which just means "call /refresh".
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Refresh token is invalid or expired");
    }
}
