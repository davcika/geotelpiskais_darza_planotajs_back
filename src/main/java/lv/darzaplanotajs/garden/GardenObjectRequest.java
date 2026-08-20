package lv.darzaplanotajs.garden;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record GardenObjectRequest(
        @NotNull ObjectType type,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = ZoneRules.MAX_GEOJSON_LENGTH) String geometry,
        @Positive Double crownRadiusM,
        @Min(1) @Max(BedGrid.MAX_DIVISIONS) Integer gridCols,
        @Min(1) @Max(BedGrid.MAX_DIVISIONS) Integer gridRows,
        Long plantId,
        @Min(1900) @Max(2200) Integer plantedYear) {

    @AssertTrue(message = "geometry type does not match the object type")
    public boolean isGeometryMatchingType() {
        return type == null || geometry == null || type.matches(geometry);
    }

    @AssertTrue(message = "a tree needs a crown radius")
    public boolean isCrownRadiusSetForTree() {
        return type != ObjectType.TREE || crownRadiusM != null;
    }

    @AssertTrue(message = "only a tree can have a catalog plant")
    public boolean isPlantOnlyForTree() {
        return type != ObjectType.BED || (plantId == null && plantedYear == null);
    }
}
