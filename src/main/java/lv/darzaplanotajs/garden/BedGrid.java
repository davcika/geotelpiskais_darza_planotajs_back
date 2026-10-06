package lv.darzaplanotajs.garden;

import org.locationtech.jts.geom.Geometry;

/**
 * Dobes režģis lokālajā rāmī, derīga ir tikai šūna, kas pilnībā atrodas dobē.
 * Frontendā ir tie paši noteikumi.
 */
public final class BedGrid {

    public static final int MAX_DIVISIONS = 100;
    /** Ja dalījums nav iestatīts, šūnas ir ap 30 cm */
    private static final double DEFAULT_CELL_M = 0.3;

    private final Geometry bed;
    private final BedFrame frame;
    private final int cols;
    private final int rows;

    public BedGrid(GardenObject bed) {
        this.bed = ZoneRules.parse(bed.getGeometry());
        this.frame = BedFrame.of(this.bed);
        this.cols = bed.getGridCols() != null ? bed.getGridCols() : defaultDivisions(frame.width);
        this.rows = bed.getGridRows() != null ? bed.getGridRows() : defaultDivisions(frame.height);
    }

    static int defaultDivisions(double lengthM) {
        return (int) Math.min(MAX_DIVISIONS, Math.max(1, Math.round(lengthM / DEFAULT_CELL_M)));
    }

    public boolean fits(int col, int row) {
        if (col >= cols || row >= rows) {
            return false;
        }
        double width = frame.width / cols;
        double height = frame.height / rows;
        return bed.covers(frame.rect(col * width, row * height, width, height));
    }
}
