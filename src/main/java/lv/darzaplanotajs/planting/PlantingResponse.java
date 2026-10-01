package lv.darzaplanotajs.planting;

import java.time.LocalDate;
import java.util.List;

public record PlantingResponse(Long id, Long objectId, Long plantId, LocalDate sownOn, String variety, String note,
                               List<List<Integer>> cells, Double blockX, Double blockY, Double blockW, Double blockH,
                               Double blockAngle,
                               Integer plantCount) {

    static PlantingResponse from(Planting p) {
        return new PlantingResponse(p.id, p.objectId, p.plantId, p.sownOn, p.variety, p.note, p.cellList(),
                p.blockX, p.blockY, p.blockW, p.blockH, p.blockAngle, p.plantCount);
    }
}
