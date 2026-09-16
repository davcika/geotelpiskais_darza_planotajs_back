package lv.darzaplanotajs.garden;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FrostRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{2}-[0-9]{2}$") String lastFrost,
        @NotBlank @Pattern(regexp = "^[0-9]{2}-[0-9]{2}$") String firstFrost) {
}
