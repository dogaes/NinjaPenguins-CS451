package com.ninjapenguins.finance.auth;

import com.ninjapenguins.finance.auth.AuthDtos.LoginRequest;
import com.ninjapenguins.finance.auth.AuthDtos.RegisterRequest;
import com.ninjapenguins.finance.auth.AuthDtos.UserResponse;
import com.ninjapenguins.finance.user.User;
import com.ninjapenguins.finance.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, UserRepository users,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Create an account (UC-AUTH-02), then sign the new user in. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body,
                                      HttpServletRequest request, HttpServletResponse response) {
        if (users.existsByUsernameIgnoreCase(body.username())) {
            return error(HttpStatus.CONFLICT, Map.of("username", "That username is taken"));
        }
        if (users.existsByEmailIgnoreCase(body.email())) {
            return error(HttpStatus.CONFLICT,
                    Map.of("email", "An account with this email may already exist. Sign in or reset your password."));
        }

        User user = new User();
        user.setFirstName(body.firstName().trim());
        user.setLastName(body.lastName().trim());
        user.setUsername(body.username().trim());
        user.setEmail(body.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(body.password()));
        users.save(user);

        signIn(user.getUsername(), body.password(), request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    /** Sign in (UC-AUTH-03) with username or email. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest body,
                                   HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication auth = signIn(body.usernameOrEmail(), body.password(), request, response);
            User user = users.findByUsernameIgnoreCase(auth.getName()).orElseThrow();
            return ResponseEntity.ok(UserResponse.from(user));
        } catch (AuthenticationException e) {
            // Same message whether the username or the password was wrong.
            return error(HttpStatus.UNAUTHORIZED, Map.of("form", "Username/email or password is incorrect."));
        }
    }

    /** Sign out (UC-AUTH-04). */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Who is signed in? The front end calls this on page load. Returns 401 if nobody is. */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        return users.findByUsernameIgnoreCase(principal.getName())
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private Authentication signIn(String login, String password,
                                  HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(login, password));
        request.changeSessionId(); // prevents session fixation
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        return auth;
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, Map<String, String> errors) {
        return ResponseEntity.status(status).body(Map.of("errors", errors));
    }
}
