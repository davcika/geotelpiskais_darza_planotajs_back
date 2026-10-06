package lv.darzaplanotajs.garden;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * Dobes lokālais rāmis, kura asis ir paralēlas dobes galvenajai malai.
 * Frontendā ir tieši tas pats, tāpēc mainot jāmaina abās vietās.
 */
public final class BedFrame {

    private static final GeometryFactory FACTORY = new GeometryFactory();

    public final double angleDeg;
    public final double width;
    public final double height;
    private final double minX, minY, ox, oy, cos, sin;

    public BedFrame(Geometry bed, double angleDeg) {
        this.angleDeg = angleDeg;
        double rad = Math.toRadians(angleDeg);
        this.cos = Math.cos(rad);
        this.sin = Math.sin(rad);
        this.minX = bed.getEnvelopeInternal().getMinX();
        this.minY = bed.getEnvelopeInternal().getMinY();
        double x0 = Double.POSITIVE_INFINITY, y0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY, y1 = Double.NEGATIVE_INFINITY;
        for (Coordinate c : bed.getCoordinates()) {
            double dx = c.x - minX, dy = c.y - minY;
            double rx = dx * cos + dy * sin, ry = -dx * sin + dy * cos;
            x0 = Math.min(x0, rx); x1 = Math.max(x1, rx);
            y0 = Math.min(y0, ry); y1 = Math.max(y1, ry);
        }
        this.ox = x0;
        this.oy = y0;
        this.width = x1 - x0;
        this.height = y1 - y0;
    }

    public static BedFrame of(Geometry bed) {
        return new BedFrame(bed, dominantAngle(bed));
    }

    /** Galvenās malas leņķis grādos, noapaļots līdz 0.1° tāpat kā frontendā */
    public static double dominantAngle(Geometry bed) {
        Geometry ring = bed instanceof Polygon p ? p.getExteriorRing() : bed;
        Coordinate[] cs = ring.getCoordinates();
        // Meklē mazākā laukuma apvilkto taisnstūri, izmēģinot katras malas leņķi
        double best = 0, bestArea = Double.POSITIVE_INFINITY;
        for (int i = 1; i < cs.length; i++) {
            double dx = cs[i].x - cs[i - 1].x, dy = cs[i].y - cs[i - 1].y;
            if (Math.hypot(dx, dy) < 1e-6) continue;
            double a = Math.atan2(dy, dx);
            double cos = Math.cos(a), sin = Math.sin(a);
            double mnx = Double.POSITIVE_INFINITY, mny = Double.POSITIVE_INFINITY;
            double mxx = Double.NEGATIVE_INFINITY, mxy = Double.NEGATIVE_INFINITY;
            for (Coordinate c : cs) {
                double rx = c.x * cos + c.y * sin, ry = -c.x * sin + c.y * cos;
                mnx = Math.min(mnx, rx); mxx = Math.max(mxx, rx);
                mny = Math.min(mny, ry); mxy = Math.max(mxy, ry);
            }
            double area = (mxx - mnx) * (mxy - mny);
            if (area < bestArea - 1e-9) {
                bestArea = area;
                best = a;
            }
        }
        double q = Math.PI / 2;
        double a = best - q * Math.round(best / q);
        if (a <= -Math.PI / 4) a += q;
        double deg = Math.round(Math.toDegrees(a) * 10) / 10.0;
        return Math.abs(deg) < 0.05 ? 0 : deg;
    }

    public double[] toLocal(double x, double y) {
        double dx = x - minX, dy = y - minY;
        return new double[] {dx * cos + dy * sin - ox, -dx * sin + dy * cos - oy};
    }

    public double[] toMap(double u, double v) {
        double rx = u + ox, ry = v + oy;
        return new double[] {minX + rx * cos - ry * sin, minY + rx * sin + ry * cos};
    }

    public Polygon rect(double x, double y, double w, double h) {
        double[][] pts = {{x, y}, {x + w, y}, {x + w, y + h}, {x, y + h}, {x, y}};
        Coordinate[] cs = new Coordinate[pts.length];
        for (int i = 0; i < pts.length; i++) {
            double[] m = toMap(pts[i][0], pts[i][1]);
            cs[i] = new Coordinate(m[0], m[1]);
        }
        return FACTORY.createPolygon(cs);
    }
}
