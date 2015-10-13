package de.slauth.auth;

public class AuthConfigException extends RuntimeException {

    public AuthConfigException() {
    }

    public AuthConfigException(String message) {
        super(message);
    }

    public AuthConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthConfigException(Throwable cause) {
        super(cause);
    }
}
