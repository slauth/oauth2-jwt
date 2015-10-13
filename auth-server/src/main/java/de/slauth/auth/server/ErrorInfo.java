package de.slauth.auth.server;

import java.util.Arrays;
import java.util.List;

public class ErrorInfo {

    private List<String> errors;

    protected ErrorInfo() {
    }

    public ErrorInfo(String... error) {
        this(Arrays.asList(error));
    }

    public ErrorInfo(List<String> errors) {
        this();
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
