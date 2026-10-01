package lv.darzaplanotajs.planting;

import java.util.List;
import lv.darzaplanotajs.garden.BedFrame;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

/**
 * Bloks ir taisnstūris metros dobes lokālajās koordinātās, un leņķis null nozīmē 0°.
 * Frontendā ir tie paši noteikumi.
 */
final class BlockRules {

    /** Pielaide, jo 5 cm solis frontendā dod piem. 0.30000000000000004 */
    static final double EPS = 1e-6;

    private BlockRules() {
    }

    record Block(double x, double y, double w, double h) {
        /** Saskarties malās drīkst */
        boolean overlaps(Block other) {
            return x + EPS < other.x + other.w && other.x + EPS < x + w
                    && y + EPS < other.y + other.h && other.y + EPS < y + h;
        }
    }

    static Geometry parseBed(String geoJson) {
        try {
            return new GeoJsonReader().read(geoJson);
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid bed geometry", e);
        }
    }

    /** Bloku pirms pārbaudes nedaudz samazina par EPS */
    static boolean insideBed(Geometry bed, Block block) {
        return insideBed(new BedFrame(bed, 0), bed, block);
    }

    static boolean insideBed(BedFrame frame, Geometry bed, Block block) {
        return bed.covers(frame.rect(block.x() + EPS, block.y() + EPS, block.w() - 2 * EPS, block.h() - 2 * EPS));
    }

    /** Pārliek bloku citā rāmī, saglabājot izmēru un centru kartē, tāpat kā frontendā */
    static Block inFrame(Block block, Double storedAngle, BedFrame frame, Geometry bed) {
        double angle = storedAngle == null ? 0 : storedAngle;
        if (Math.abs(angle - frame.angleDeg) < 1e-9) return block;
        double[] c = new BedFrame(bed, angle).toMap(block.x() + block.w() / 2, block.y() + block.h() / 2);
        double[] l = frame.toLocal(c[0], c[1]);
        return new Block(snap(l[0] - block.w() / 2), snap(l[1] - block.h() / 2), block.w(), block.h());
    }

    private static double snap(double v) {
        return Math.round(v / 0.05) * 0.05;
    }

    static boolean overlapsAny(Block block, List<Block> others) {
        return others.stream().anyMatch(block::overlaps);
    }
}
