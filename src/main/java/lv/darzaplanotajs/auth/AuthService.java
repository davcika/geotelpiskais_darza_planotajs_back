package lv.darzaplanotajs.auth;

import lv.darzaplanotajs.common.ApiErrors;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    /** Izstrādes konts no migrācijas V2 */
    static final String DEV_EMAIL = "dev@darza-planotajs.lv";
    private static final Duration TOKEN_LIFETIME = Duration.ofDays(7);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final boolean devLoginEnabled;

    public AuthService(AppUserRepository users, PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder, @Value("${app.dev-login.enabled}") boolean devLoginEnabled) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.devLoginEnabled = devLoginEnabled;
    }

    /** Dārzu lietotājs pēc tam izveido pats sākuma ekrānā */
    @Transactional
    public AuthResponse register(AuthRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw ApiErrors.conflict("A user with this email already exists");
        }
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.password())));
        return issueToken(user);
    }

    public AuthResponse login(AuthRequest request) {
        return users.findByEmail(normalize(request.email()))
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(this::issueToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
    }

    /** Ieeja izstrādes kontā bez paroles, strādā tikai dev profilā */
    public AuthResponse devLogin() {
        if (!devLoginEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dev login is disabled; start the backend with the dev profile");
        }
        return users.findByEmail(DEV_EMAIL)
                .map(this::issueToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dev account not found"));
    }

    private AuthResponse issueToken(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(TOKEN_LIFETIME))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponse(token, user.getEmail());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
