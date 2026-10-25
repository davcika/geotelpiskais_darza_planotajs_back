package lv.darzaplanotajs.soil;

import java.util.ArrayList;
import java.util.List;
import lv.darzaplanotajs.plant.PlantResponse;

/** Cik augs der augsnei, bez Spring un DB, lai būtu viegli testēt */
public final class SoilSuitability {

    public enum Level { GOOD, OK, POOR }

    /** checks ir cik pārbaužu varēja izdarīt, 0 nozīmē, ka nav datu */
    public record Verdict(Level level, List<String> reasons, int checks) {
    }

    private static final double PH_TOLERANCE = 0.5;

    private SoilSuitability() {
    }

    public static Verdict assess(PlantResponse plant, SoilResponse soil) {
        List<String> reasons = new ArrayList<>();
        Level level = Level.GOOD;
        int checks = 0;
        if (soil == null) {
            return new Verdict(Level.OK, List.of("No soil data"), 0);
        }
        if (soil.ph() != null && plant.phMin() != null && plant.phMax() != null) {
            checks++;
            double ph = soil.ph();
            double off = ph < plant.phMin() ? plant.phMin() - ph : ph > plant.phMax() ? ph - plant.phMax() : 0;
            String range = "pH " + ph + " (likes " + plant.phMin() + "–" + plant.phMax() + ")";
            if (off == 0) {
                reasons.add(range + " fits");
            } else if (off <= PH_TOLERANCE + 1e-9) {
                level = worse(level, Level.OK);
                reasons.add(range + (ph < plant.phMin() ? " slightly too acidic" : " slightly too alkaline"));
            } else {
                level = worse(level, Level.POOR);
                reasons.add(range + (ph < plant.phMin() ? " too acidic — lime it" : " too alkaline"));
            }
        }
        if (soil.light() != null && plant.light() != null) {
            checks++;
            int diff = rank(soil.light()) - rank(plant.light());
            if (diff == 0) {
                reasons.add("Light " + soil.light() + " fits");
            } else if (Math.abs(diff) == 2) {
                level = worse(level, Level.POOR);
                reasons.add(diff < 0 ? "Needs sun, bed is shaded" : "Prefers shade, bed is in full sun");
            } else {
                level = worse(level, Level.OK);
                reasons.add(diff < 0 ? "Wants more light (" + plant.light() + ")" : "Prefers less sun (" + plant.light() + ")");
            }
        }
        if (soil.organicMatterPct() != null && "VEGETABLE".equals(plant.category()) && soil.organicMatterPct() < 2) {
            level = worse(level, Level.OK);
            reasons.add("Low organic matter (" + soil.organicMatterPct() + "%) — add compost");
        }
        if ("WET".equals(soil.moisture()) && ("TREE".equals(plant.category()) || "BULB".equals(plant.category()))) {
            level = worse(level, Level.OK);
            reasons.add("Wet soil — risk of root/bulb rot");
        }
        if (checks == 0 && reasons.isEmpty()) {
            return new Verdict(Level.OK, List.of("Not enough soil or plant data to compare"), 0);
        }
        return new Verdict(level, reasons, checks);
    }

    private static int rank(String light) {
        return switch (light) {
            case "SUN" -> 2;
            case "PART" -> 1;
            default -> 0;
        };
    }

    private static Level worse(Level a, Level b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
