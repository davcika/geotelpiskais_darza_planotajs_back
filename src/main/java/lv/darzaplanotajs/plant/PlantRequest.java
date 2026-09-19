package lv.darzaplanotajs.plant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Tie paši noteikumi kā DB CHECK.
 * Ja shareWithEveryone ir true, augs gaida admina apstiprinājumu, citādi paliek privāts.
 */
public record PlantRequest(
        @NotBlank @Size(max = 60) String nameEn,
        @NotBlank @Size(max = 60) String nameLv,
        @NotBlank @Size(max = 30) @Pattern(regexp = "[A-Za-z]+") String family,
        @NotBlank @Pattern(regexp = "VEGETABLE|HERB|FLOWER|BULB|SHRUB|TREE|HEDGE") String category,
        @NotBlank @Pattern(regexp = "DIRECT|TRANSPLANT|PLANT") String sowMethod,
        @Pattern(regexp = MD) String indoorSowFrom,
        @Pattern(regexp = MD) String indoorSowTo,
        @NotBlank @Pattern(regexp = MD) String outdoorFrom,
        @NotBlank @Pattern(regexp = MD) String outdoorTo,
        @Positive Integer daysToHarvest,
        @Positive Integer harvestWindowDays,
        boolean frostSensitive,
        boolean perennial,
        boolean evergreen,
        @Pattern(regexp = MD) String foliageFrom,
        @Pattern(regexp = MD) String foliageTo,
        @Pattern(regexp = MD) String bloomFrom,
        @Pattern(regexp = MD) String bloomTo,
        @Pattern(regexp = "|#[0-9a-f]{6}") String bloomColor,
        @Positive Double heightM,
        @Positive Double spreadM,
        @Positive Integer yearsToMature,
        @DecimalMin("3") @DecimalMax("9") Double phMin,
        @DecimalMin("3") @DecimalMax("9") Double phMax,
        @Pattern(regexp = "|SUN|PART|SHADE") String light,
        boolean shareWithEveryone,
        @Positive Integer spacingCm,
        @Positive Integer rowSpacingCm,
        @Size(max = 40) String icon) {

    /** Mēnesis un diena vai tukšs */
    static final String MD = "|\\d{2}-\\d{2}";
}
