package lv.darzaplanotajs.garden;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Punkts WGS84, frontend to pārrēķina no EPSG 3059 */
public record FrostComputeRequest(
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double lon) {
}
