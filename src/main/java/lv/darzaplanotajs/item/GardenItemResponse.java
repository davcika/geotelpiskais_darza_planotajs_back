package lv.darzaplanotajs.item;

import java.time.Instant;

/** lengthM tikai LINE elementiem, areaM2 tikai AREA elementiem (citādi null) */
public record GardenItemResponse(long id, long gardenId, String typeKey, String geometry, double rotationDeg,
                                 String label, Instant createdAt, Double lengthM, Double areaM2) {
}
