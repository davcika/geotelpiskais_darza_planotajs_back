package lv.darzaplanotajs.garden;

/** Jaunam veidam jāatjauno arī frontend un CHECK jaunā migrācijā */
public enum ObjectType {
    BED("Polygon"),
    TREE("Point");

    private final String geometryType;

    ObjectType(String geometryType) {
        this.geometryType = geometryType;
    }

    /** Vai GeoJSON ģeometrijas tips atbilst šim objekta veidam (nederīgs GeoJSON neatbilst) */
    public boolean matches(String geoJson) {
        try {
            return ZoneRules.parse(geoJson).getGeometryType().equals(geometryType);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
