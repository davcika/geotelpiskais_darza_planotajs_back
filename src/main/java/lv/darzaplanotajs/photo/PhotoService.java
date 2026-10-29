package lv.darzaplanotajs.photo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import lv.darzaplanotajs.common.ApiErrors;
import lv.darzaplanotajs.garden.GardenService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/** Piederību pārbauda caur stādījuma dobi, svešai bildei 404 */
@Service
@Transactional
public class PhotoService {

    static final long MAX_BYTES = 5L * 1024 * 1024;
    static final int MAX_PER_PLANTING = 5;

    private final PlantingPhotoRepository photos;
    private final PhotoStorage storage;
    private final GardenService gardens;
    private final JdbcTemplate jdbc;

    PhotoService(PlantingPhotoRepository photos, PhotoStorage storage, GardenService gardens, JdbcTemplate jdbc) {
        this.photos = photos;
        this.storage = storage;
        this.gardens = gardens;
        this.jdbc = jdbc;
    }

    public PhotoResponse upload(long userId, long plantingId, MultipartFile file) {
        requireOwnPlanting(userId, plantingId);
        if (file == null || file.isEmpty()) {
            throw ApiErrors.badRequest("File is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Photo must be at most 5 MB");
        }
        if (photos.countByPlantingId(plantingId) >= MAX_PER_PLANTING) {
            throw ApiErrors.badRequest("At most " + MAX_PER_PLANTING + " photos per planting");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ImageType type = ImageType.sniff(bytes)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Only JPEG, PNG or WebP images are allowed"));
        String key = storage.save(bytes);
        // ja transakcija atceļas, failu izdzēš uzreiz
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    storage.delete(key);
                }
            }
        });
        return PhotoResponse.from(photos.save(
                new PlantingPhoto(plantingId, type.mime, bytes.length, key, cleanName(file.getOriginalFilename()))));
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> list(long userId, long plantingId) {
        requireOwnPlanting(userId, plantingId);
        return photos.findByPlantingIdOrderById(plantingId).stream().map(PhotoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Loaded load(long userId, long photoId) {
        PlantingPhoto photo = findOwn(userId, photoId);
        try {
            return new Loaded(photo.contentType, storage.read(photo.storageKey));
        } catch (UncheckedIOException e) {
            throw ApiErrors.notFound("Photo", photoId);
        }
    }

    public void delete(long userId, long photoId) {
        PlantingPhoto photo = findOwn(userId, photoId);
        photos.delete(photo);
        String key = photo.storageKey;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                storage.delete(key);
            }
        });
    }

    public record Loaded(String contentType, byte[] bytes) {
    }

    private PlantingPhoto findOwn(long userId, long photoId) {
        PlantingPhoto photo = photos.findById(photoId).orElseThrow(() -> ApiErrors.notFound("Photo", photoId));
        try {
            requireOwnPlanting(userId, photo.plantingId);
        } catch (ResponseStatusException e) {
            throw ApiErrors.notFound("Photo", photoId);
        }
        return photo;
    }

    /** Planting entītija nav publiska, tāpēc dobes id nolasa tieši no tabulas */
    private void requireOwnPlanting(long userId, long plantingId) {
        List<Long> objectIds = jdbc.queryForList(
                "select garden_object_id from planting where id = ?", Long.class, plantingId);
        if (objectIds.isEmpty()) {
            throw ApiErrors.notFound("Planting", plantingId);
        }
        gardens.findOwnObject(userId, objectIds.get(0));
    }

    private static String cleanName(String name) {
        if (name == null) {
            return null;
        }
        String base = name.replaceAll(".*[/\\\\]", "").replaceAll("[\\p{Cntrl}]", "").strip();
        if (base.isEmpty()) {
            return null;
        }
        return base.length() > 255 ? base.substring(0, 255) : base;
    }
}
