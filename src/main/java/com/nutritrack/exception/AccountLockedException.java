package com.nutritrack.exception;

/**
 * Thrown when a login attempt is made against an account that is currently
 * locked out because of too many consecutive failed password attempts.
 */
public class AccountLockedException extends RuntimeException {
    private final long secondsRemaining;

    public AccountLockedException(long secondsRemaining) {
        super("Account temporarily locked due to too many failed login attempts");
        this.secondsRemaining = secondsRemaining;
    }

    public long getSecondsRemaining() {
        return secondsRemaining;
    }
}
