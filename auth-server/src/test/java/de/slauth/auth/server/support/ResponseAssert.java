package de.slauth.auth.server.support;

import org.assertj.core.api.AbstractAssert;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.Response;

public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

    protected ResponseAssert(Response actual) {
        super(actual, ResponseAssert.class);
    }

    public ResponseAssert hasStatus(int expected) {
        isNotNull();
        if (actual.getStatus() != expected) {
            failWithMessage("Expected status to be <%s> but was <%s>", expected, actual.getStatus());
        }
        return this;
    }

    public ResponseAssert hasStatusOk() {
        return hasStatus(HttpStatus.OK.value());
    }

    public ResponseAssert hasStatusCreated() {
        return hasStatus(HttpStatus.CREATED.value());
    }

    public ResponseAssert hasStatusNoContent() {
        return hasStatus(HttpStatus.NO_CONTENT.value());
    }

    public ResponseAssert hasStatusBadRequest() {
        return hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    public ResponseAssert hasStatusUnauthorized() {
        return hasStatus(HttpStatus.UNAUTHORIZED.value());
    }

    public ResponseAssert hasStatusForbidden() {
        return hasStatus(HttpStatus.FORBIDDEN.value());
    }
}
