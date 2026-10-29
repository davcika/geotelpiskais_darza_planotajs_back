package lv.darzaplanotajs.photo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Pats fails glabājas diskā zem storageKey */
@Entity
@Table(name = "planting_photo")
public class PlantingPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long plantingId;

    @Column(nullable = false)
    String contentType;

    @Column(nullable = false)
    long sizeBytes;

    @Column(nullable = false)
    String storageKey;

    String originalName;

    @Column(nullable = false, updatable = false)
    OffsetDateTime createdAt;

    protected PlantingPhoto() {
    }

    PlantingPhoto(Long plantingId, String contentType, long sizeBytes, String storageKey, String originalName) {
        this.plantingId = plantingId;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
        this.originalName = originalName;
        this.createdAt = OffsetDateTime.now();
    }
}
