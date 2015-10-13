package de.slauth.auth.server;

import de.slauth.auth.User;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.net.URI;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
@Api(value = "users")
public class UserController {

    private static final String ACCESS_CONTROL = "hasAnyRole('ROLE_ADMIN', 'ROLE_USER_MANAGEMENT')";

    @Autowired UserDetailsService userDetailsService;

    @RequestMapping(method = POST)
    @PreAuthorize(ACCESS_CONTROL)
    @ApiOperation(value = "Creates a new user")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Success"),
            @ApiResponse(code = 400, message = "A user with the given name already exists"),
    })
    public ResponseEntity<Void> createUser(@RequestBody @Valid User user) {
        if (userDetailsService.exists(user.getUsername())) {
            throw new IllegalArgumentException(String.format("Username '%s' already exists", user.getUsername()));
        }
        User savedUser = userDetailsService.save(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{username}")
                .buildAndExpand(savedUser.getUsername())
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<>(headers, CREATED);
    }

    @RequestMapping(method = GET, value = "/me")
    @ApiOperation("Provides details about the user associated with the given access token")
    public User getMe(
            @ApiIgnore
            @AuthenticationPrincipal User me
    ) {
        return me;
    }

    @PreAuthorize(ACCESS_CONTROL)
    @RequestMapping(method = GET, value = "/{username}")
    @ApiOperation("Provides details about the user with the given name")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No user with the given name exists"),
    })
    public User getUser(@PathVariable String username) {
        return userDetailsService.loadUserByUsername(username);
    }

    @PreAuthorize(ACCESS_CONTROL)
    @RequestMapping(method = DELETE, value = "/{username}")
    @ResponseStatus(NO_CONTENT)
    @ApiOperation("Deletes the user with the given name")
    public void deleteUser(@PathVariable String username) {
        if (userDetailsService.exists(username)) {
            userDetailsService.delete(username);
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
    @ExceptionHandler(UsernameNotFoundException.class)
    public void usernameNotFoundException() {}

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorInfo illegalArgumentException(IllegalArgumentException e) {
        return new ErrorInfo(e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorInfo methodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ErrorInfo(e.getBindingResult().getFieldErrors().stream()
                .map(error -> "'" + error.getField() + "' " + error.getDefaultMessage())
                .collect(Collectors.toList()));
    }
}
