package lv.darzaplanotajs.photo;

import java.time.OffsetDateTime;

public record PhotoResponse(long id, long plantingId, String contentType, long sizeBytes,
                            String originalName, OffsetDateTime createdAt) {

    static PhotoResponse from(PlantingPhoto p) {
        return new PhotoResponse(p.id, p.plantingId, p.contentType, p.sizeBytes, p.originalName, p.createdAt);
    }
}
