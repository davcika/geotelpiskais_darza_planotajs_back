package lv.darzaplanotajs.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GardenItemRequest(
        @NotBlank @Size(max = 30) String typeKey,
        @NotBlank @Size(max = 100_000) String geometry,
        @DecimalMin("0") @DecimalMax(value = "360", inclusive = false) Double rotationDeg,
        @Size(max = 100) String label) {
}
