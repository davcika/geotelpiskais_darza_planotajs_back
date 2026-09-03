package lv.darzaplanotajs.common;

import org.springframework.security.oauth2.jwt.Jwt;

/** Lietotāja id no JWT subject */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static long userId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
