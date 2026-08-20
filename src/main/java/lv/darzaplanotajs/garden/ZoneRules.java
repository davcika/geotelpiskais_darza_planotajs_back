package lv.darzaplanotajs.garden;

import lv.darzaplanotajs.common.ApiErrors;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

/** Koordinātas ir metros, tāpēc JTS plaknes aprēķini der bez ģeodēziskām korekcijām */
final class ZoneRules {

    static final double MIN_AREA_M2 = 20;
    static final double MAX_AREA_M2 = 50_000;
    /** GeoJSON teksta garuma limits pieprasījumos */
    static final int MAX_GEOJSON_LENGTH = 100_000;

    private ZoneRules() {
    }

    /** Viens poligons bez caurumiem un pašķrustošanās, ar pieļaujamu laukumu */
    static Polygon validZone(String geoJson) {
        if (!(parse(geoJson) instanceof Polygon zone) || zone.getNumInteriorRing() > 0) {
            throw ApiErrors.badRequest("Zone must be a single polygon without holes");
        }
        if (!zone.isValid()) {
            throw ApiErrors.badRequest("Zone must not intersect itself");
        }
        if (zone.getArea() < MIN_AREA_M2 || zone.getArea() > MAX_AREA_M2) {
            throw ApiErrors.badRequest("Zone area must be between %.0f m² and %.0f m²".formatted(MIN_AREA_M2, MAX_AREA_M2));
        }
        return zone;
    }

    /** Kokam pārbauda tikai stumbru, tāpēc vainags drīkst pārkarāties pāri robežai kā īstam kokam pāri žogam */
    static boolean contains(Geometry zone, String objectGeoJson) {
        Geometry object = parse(objectGeoJson);
        if (!object.isValid()) {
            throw ApiErrors.badRequest("Object geometry must not intersect itself");
        }
        return zone.covers(object);
    }

    static Geometry parse(String geoJson) {
        try {
            return new GeoJsonReader().read(geoJson);
        } catch (ParseException | RuntimeException e) {
            throw ApiErrors.badRequest("Geometry is not valid GeoJSON");
        }
    }
}
