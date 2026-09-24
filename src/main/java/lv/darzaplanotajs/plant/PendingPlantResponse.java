package lv.darzaplanotajs.plant;

import java.time.Instant;
import java.util.List;

public record PendingPlantResponse(PlantResponse plant, String ownerEmail, Instant submittedAt,
                                   List<SimilarPlant> similar) {
}
