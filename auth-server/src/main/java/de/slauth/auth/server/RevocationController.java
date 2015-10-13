package de.slauth.auth.server;

import de.slauth.auth.RevocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/revocations", produces = APPLICATION_JSON_VALUE)
@Api(value = "revocations")
public class RevocationController {

    @Autowired
    RevocationService revocationService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation("Lists all revoked tokens")
    public Set<String> getRevocations() {
        return revocationService.getRevokedTokens();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = POST, consumes = TEXT_PLAIN_VALUE)
    @ResponseStatus(NO_CONTENT)
    @ApiOperation("Revokes the given token")
    public void revoke(@RequestBody String token) {
        revocationService.revoke(token);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = DELETE)
    @ResponseStatus(NO_CONTENT)
    @ApiOperation(value = "Clears the revocation list", notes = "All revoked tokens will be valid again after this " +
            "operation (except they are already expired).")
    public void clearRevocations() {
        revocationService.clearRevokedTokens();
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTokenException.class)
    public String handleException(InvalidTokenException e) {
        return e.getMessage();
    }
}
