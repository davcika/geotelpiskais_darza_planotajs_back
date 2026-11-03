package lv.darzaplanotajs.item;

import lv.darzaplanotajs.common.ApiErrors;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

/** ZoneRules nav publisks, tāpēc tā pati JTS covers pārbaude ir arī šeit */
final class ItemGeometry {

    private ItemGeometry() {
    }

    static Geometry parse(String geoJson) {
        try {
            return new GeoJsonReader().read(geoJson);
        } catch (ParseException | RuntimeException e) {
            throw ApiErrors.badRequest("Geometry is not valid GeoJSON");
        }
    }

    /** AREA ir Polygon, POINT ir Point un LINE ir LineString */
    static Geometry validFor(String shape, String geoJson) {
        Geometry g = parse(geoJson);
        boolean ok = switch (shape) {
            case "AREA" -> g instanceof Polygon;
            case "POINT" -> g instanceof Point;
            case "LINE" -> g instanceof LineString && g.getLength() > 0;
            default -> false;
        };
        if (!ok) {
            throw ApiErrors.badRequest("Geometry does not match item shape " + shape);
        }
        if (!g.isValid()) {
            throw ApiErrors.badRequest("Item geometry must not intersect itself");
        }
        return g;
    }

    /** Dārzs bez zonas elementus neierobežo */
    static boolean insideZone(String hubGeoJson, Geometry item) {
        return hubGeoJson == null || parse(hubGeoJson).covers(item);
    }
}
