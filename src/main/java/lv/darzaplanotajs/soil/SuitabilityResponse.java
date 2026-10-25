package lv.darzaplanotajs.soil;

import java.util.List;

/** Dobes stādījumu vērtējums un kataloga augi, kas der dobes augsnei */
public record SuitabilityResponse(SoilResponse soil, List<Item> plantings, List<Item> suggestions) {

    public record Item(Long plantingId, Long plantId, String nameEn, String nameLv, String level, List<String> reasons) {
    }
}
