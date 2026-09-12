package lv.darzaplanotajs.garden;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GardenRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Pattern(regexp = Garden.ICON_PATTERN, message = "Unknown icon") String icon,
        @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Color must be #rrggbb") String color) {
}
