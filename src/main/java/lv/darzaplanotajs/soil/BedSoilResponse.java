package lv.darzaplanotajs.soil;

/** own var nebūt, effective ir own vai dārza noklusējums */
public record BedSoilResponse(long gardenId, SoilResponse own, SoilResponse gardenDefault, SoilResponse effective) {
}
