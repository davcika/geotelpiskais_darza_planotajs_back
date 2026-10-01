package lv.darzaplanotajs.planting;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/** Bloka četriem laukiem jābūt visiem vai nevienam, un blockAngle null nozīmē 0° */
public record PlantingRequest(
        @NotNull Long plantId,
        @NotNull LocalDate sownOn,
        @Size(max = 60) String variety,
        @Size(max = 200) String note,
        @Size(max = 2000) List<@NotNull @Size(min = 2, max = 2) List<@NotNull @Min(0) @Max(9999) Integer>> cells,
        @DecimalMin("0") @DecimalMax("1000") Double blockX,
        @DecimalMin("0") @DecimalMax("1000") Double blockY,
        @DecimalMin(value = "0", inclusive = false) @DecimalMax("1000") Double blockW,
        @DecimalMin(value = "0", inclusive = false) @DecimalMax("1000") Double blockH,
        @DecimalMin("-45") @DecimalMax("45") Double blockAngle,
        @Min(1) @Max(100000) Integer plantCount) {

    @AssertTrue(message = "cells must not repeat")
    public boolean isCellsDistinct() {
        return cells == null || cells.stream().distinct().count() == cells.size();
    }

    @AssertTrue(message = "block needs all of blockX, blockY, blockW, blockH or none")
    public boolean isBlockComplete() {
        long given = Stream.of(blockX, blockY, blockW, blockH).filter(v -> v != null).count();
        return given == 0 || given == 4;
    }

    BlockRules.Block block() {
        return blockX == null || blockY == null || blockW == null || blockH == null
                ? null : new BlockRules.Block(blockX, blockY, blockW, blockH);
    }
}
