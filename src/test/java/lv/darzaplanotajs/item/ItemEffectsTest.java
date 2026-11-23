package lv.darzaplanotajs.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

class ItemEffectsTest {

    private static final GeometryFactory F = new GeometryFactory();
    private static final ItemType GREENHOUSE = type("GREENHOUSE", "AREA", 3.0, 35, 30, 30, 3);
    private static final ItemType COLD_FRAME = type("COLD_FRAME", "AREA", 1.2, 21, 21, 21, 2);
    private static final ItemType CLOCHE = type("CLOCHE", "POINT", 0.4, 14, 0, 0, 2);
    private static final ItemType FENCE = type("FENCE", "LINE", null, 0, 0, 0, 0);

    private static ItemType type(String key, String shape, Double w, int s, int e, int h, double frost) {
        return new ItemType(key, key, key, "x", shape, "PROTECTION", w, w, s, e, h, frost, false);
    }

    private static Geometry rect(double x, double y, double w, double h) {
        return F.toGeometry(new Envelope(x, x + w, y, y + h));
    }

    @Test
    void coversWhenAtLeastHalf() {
        Geometry bed = rect(0, 0, 4, 1);
        var half = new ItemEffects.PlacedItem(1, COLD_FRAME, rect(0, 0, 2, 1));
        var less = new ItemEffects.PlacedItem(2, COLD_FRAME, rect(0, 0, 1.9, 1));
        assertEquals(List.of(half), ItemEffects.covering(bed, List.of(half, less)));
    }

    @Test
    void ignoresNonProtectionItems() {
        var fence = new ItemEffects.PlacedItem(1, FENCE,
                F.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(5, 0)}));
        assertTrue(ItemEffects.covering(rect(0, 0, 1, 1), List.of(fence)).isEmpty());
    }

    @Test
    void pointItemCoversSmallBlockAndPoint() {
        var cloche = new ItemEffects.PlacedItem(1, CLOCHE, F.createPoint(new Coordinate(0.2, 0.2)));
        assertEquals(1, ItemEffects.covering(rect(0.1, 0.1, 0.2, 0.2), List.of(cloche)).size());
        assertEquals(1, ItemEffects.covering(F.createPoint(new Coordinate(0.25, 0.2)), List.of(cloche)).size());
        assertTrue(ItemEffects.covering(rect(0, 0, 3, 3), List.of(cloche)).isEmpty());
    }

    @Test
    void shiftsWindowsTakingLargestShift() {
        var gh = new ItemEffects.PlacedItem(1, GREENHOUSE, rect(0, 0, 1, 1));
        var cf = new ItemEffects.PlacedItem(2, COLD_FRAME, rect(0, 0, 1, 1));
        var adj = ItemEffects.adjust("04-15", "06-30", 2026, List.of(cf, gh));
        assertEquals("03-11", adj.outdoorFrom());
        assertEquals("07-30", adj.outdoorTo());
        assertEquals(30, adj.harvestExtraDays());
        assertEquals(3.0, adj.frostProtectionC());
    }

    @Test
    void clampsToCalendarYear() {
        var gh = new ItemEffects.PlacedItem(1, GREENHOUSE, rect(0, 0, 1, 1));
        var adj = ItemEffects.adjust("01-20", "12-15", 2026, List.of(gh));
        assertEquals("01-01", adj.outdoorFrom());
        assertEquals("12-31", adj.outdoorTo());
    }

    @Test
    void noItemsNoChange() {
        var adj = ItemEffects.adjust("05-01", "06-01", 2026, List.of());
        assertEquals("05-01", adj.outdoorFrom());
        assertEquals("06-01", adj.outdoorTo());
        assertEquals(0, adj.harvestExtraDays());
    }
}
