package lv.darzaplanotajs.photo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Faila vārds vienmēr ir servera UUID, klienta vārdu ceļā nelieto */
@Component
class PhotoStorage {

    private static final Pattern KEY = Pattern.compile("[0-9a-f]{32}");

    private final Path dir;

    PhotoStorage(@Value("${app.photos.dir:${user.home}/.darza-planotajs/photos}") String dir) {
        this.dir = Path.of(dir).toAbsolutePath().normalize();
    }

    Path dir() {
        return dir;
    }

    String save(byte[] bytes) {
        String key = UUID.randomUUID().toString().replace("-", "");
        try {
            Files.createDirectories(dir);
            Path tmp = Files.createTempFile(dir, "up-", ".tmp");
            Files.write(tmp, bytes);
            Files.move(tmp, resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return key;
    }

    byte[] read(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            // nav kritiski, bāreņu tīrītājs mēģinās vēlreiz
        }
    }

    static boolean isKey(String name) {
        return KEY.matcher(name).matches();
    }

    private Path resolve(String key) {
        if (!isKey(key)) {
            throw new IllegalArgumentException("Bad storage key");
        }
        Path p = dir.resolve(key).normalize();
        if (!p.startsWith(dir)) {
            throw new IllegalArgumentException("Bad storage key");
        }
        return p;
    }
}
