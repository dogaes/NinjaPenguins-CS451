package com.ninjapenguins.finance.auth;

import com.ninjapenguins.finance.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request and response bodies for /api/auth. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "Enter your username or email") String usernameOrEmail,
            @NotBlank(message = "Enter your password") String password) {
    }

    public record RegisterRequest(
            @NotBlank(message = "First name is required") @Size(max = 100) String firstName,
            @NotBlank(message = "Last name is required") @Size(max = 100) String lastName,
            @NotBlank(message = "Username is required")
            @Pattern(regexp = "^[A-Za-z0-9_.]{3,50}$",
                    message = "Username must be 3-50 characters: letters, numbers, _ or .")
            String username,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 255)
            String email,
            // Password policy from the SRS (FR-AUTH-003): 8+ characters, at least one letter and one number.
            @NotBlank(message = "Password is required")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                    message = "Password must be at least 8 characters and include a letter and a number")
            String password) {
    }

    /** What the front end gets back. Never includes the password hash. */
    public record UserResponse(Long id, String firstName, String lastName, String username, String email) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getUsername(), u.getEmail());
        }
    }
}
