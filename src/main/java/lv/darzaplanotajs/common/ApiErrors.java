package lv.darzaplanotajs.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ApiErrors {

    private ApiErrors() {
    }

    public static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    /** Arī svešiem resursiem, lai neatklātu, ka tie pastāv */
    public static ResponseStatusException notFound(String what, long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " " + id + " not found");
    }

    public static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public static ResponseStatusException unavailable(String message) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    /** Lietotājs dārzu redz, bet darbība atļauta tikai īpašniekam */
    public static ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
