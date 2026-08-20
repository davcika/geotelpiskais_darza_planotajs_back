package lv.darzaplanotajs.garden;

public record GardenObjectResponse(
        Long id,
        ObjectType type,
        String name,
        String geometry,
        Double crownRadiusM,
        Integer gridCols,
        Integer gridRows,
        Long plantId,
        Integer plantedYear) {

    static GardenObjectResponse from(GardenObject object) {
        return new GardenObjectResponse(
                object.getId(),
                object.getType(),
                object.getName(),
                object.getGeometry(),
                object.getCrownRadiusM(),
                object.getGridCols(),
                object.getGridRows(),
                object.getPlantId(),
                object.getPlantedYear());
    }
}
