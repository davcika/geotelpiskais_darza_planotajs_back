package lv.darzaplanotajs.photo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Dzēš failus bez DB rindas, bet pavisam svaigus neaiztiek, lai netraucētu augšupielādei */
@Component
class OrphanPhotoCleaner {

    private static final Logger log = LoggerFactory.getLogger(OrphanPhotoCleaner.class);

    private final PhotoStorage storage;
    private final PlantingPhotoRepository photos;

    OrphanPhotoCleaner(PhotoStorage storage, PlantingPhotoRepository photos) {
        this.storage = storage;
        this.photos = photos;
    }

    @Scheduled(initialDelayString = "PT5M", fixedDelayString = "${app.photos.cleanup-interval:PT6H}")
    void clean() {
        Path dir = storage.dir();
        if (!Files.isDirectory(dir)) {
            return;
        }
        Set<String> known = new HashSet<>(photos.findAllStorageKeys());
        Instant cutoff = Instant.now().minus(Duration.ofHours(1));
        int removed = 0;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path f : (Iterable<Path>) files::iterator) {
                String name = f.getFileName().toString();
                boolean ours = PhotoStorage.isKey(name) || name.endsWith(".tmp");
                if (!ours || known.contains(name) || Files.getLastModifiedTime(f).toInstant().isAfter(cutoff)) {
                    continue;
                }
                Files.deleteIfExists(f);
                removed++;
            }
        } catch (IOException e) {
            log.warn("Photo cleanup failed: {}", e.getMessage());
        }
        if (removed > 0) {
            log.info("Removed {} orphan photo files", removed);
        }
    }
}
