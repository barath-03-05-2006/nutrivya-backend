package com.nutritrack.exception;

/**
 * Thrown for a wrong email/password combination. Carries how many attempts
 * remain before the account gets locked out, so the UI can warn the user.
 * attemptsRemaining is -1 when it doesn't apply (e.g. unknown email).
 */
public class InvalidCredentialsException extends RuntimeException {
    private final int attemptsRemaining;

    public InvalidCredentialsException(int attemptsRemaining) {
        super("Invalid email or password");
        this.attemptsRemaining = attemptsRemaining;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }
}
