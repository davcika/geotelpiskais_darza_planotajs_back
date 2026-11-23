package lv.darzaplanotajs.planting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

class BlockRulesTest {

    /** L veida dobe, 2 × 2 m kvadrāts bez augšējā labā 1 × 1 m stūra */
    private static final Geometry L_BED = BlockRules.parseBed("""
            {"type":"Polygon","coordinates":[[[500000,300000],[500002,300000],[500002,300001],
            [500001,300001],[500001,300002],[500000,300002],[500000,300000]]]}""");

    @Test
    void blockInsideBed() {
        assertTrue(BlockRules.insideBed(L_BED, new BlockRules.Block(0, 0, 2, 1)));
        assertTrue(BlockRules.insideBed(L_BED, new BlockRules.Block(0.1 + 0.2, 0, 0.7, 2)));
    }

    @Test
    void blockOutsideBedOrInCutCorner() {
        assertFalse(BlockRules.insideBed(L_BED, new BlockRules.Block(1.5, 1.5, 0.3, 0.3)));
        assertFalse(BlockRules.insideBed(L_BED, new BlockRules.Block(0, 0, 2.1, 1)));
    }

    @Test
    void touchingBlocksDoNotOverlap() {
        BlockRules.Block a = new BlockRules.Block(0, 0, 1, 1);
        assertFalse(BlockRules.overlapsAny(a, List.of(new BlockRules.Block(1, 0, 1, 1))));
        assertTrue(BlockRules.overlapsAny(a, List.of(new BlockRules.Block(0.95, 0.5, 1, 1))));
    }
}
