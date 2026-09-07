package lv.darzaplanotajs.address;

/** east un north ir EPSG 3059 metros */
public record AddressResponse(long code, String label, double east, double north) {
}
