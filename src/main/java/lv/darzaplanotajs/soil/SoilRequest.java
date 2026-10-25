package lv.darzaplanotajs.soil;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Visi lauki nav obligāti, N P K ir mg/kg no laboratorijas */
public record SoilRequest(
        @Pattern(regexp = "SAND|SANDY_LOAM|LOAM|CLAY_LOAM|CLAY|PEAT") String soilType,
        @DecimalMin("3.0") @DecimalMax("10.0") Double ph,
        @DecimalMin("0") @DecimalMax("100") Double organicMatterPct,
        @DecimalMin("0") @DecimalMax("10000") Double nitrogen,
        @DecimalMin("0") @DecimalMax("10000") Double phosphorus,
        @DecimalMin("0") @DecimalMax("10000") Double potassium,
        @Pattern(regexp = "DRY|MEDIUM|WET") String moisture,
        @Pattern(regexp = "SUN|PART|SHADE") String light,
        LocalDate testedOn,
        @Size(max = 500) String labNote) {
}
