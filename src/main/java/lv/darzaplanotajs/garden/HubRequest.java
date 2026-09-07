package lv.darzaplanotajs.garden;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Adrese ir null, ja lietotājs to izlaida */
public record HubRequest(
        Long addressCode,
        @NotBlank @Size(max = ZoneRules.MAX_GEOJSON_LENGTH) String geometry) {
}
