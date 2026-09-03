package lv.darzaplanotajs.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record AuthRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8) String password) {

    /** Bcrypt pieņem līdz 72 baitiem, un ā aizņem divus, tāpēc skaita baitus */
    @AssertTrue(message = "password is too long")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
