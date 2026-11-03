package lv.darzaplanotajs.item;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/** Vairāki elementi nesummējas, ņem lielāko nobīdi, jo siltumnīca lecektī nedod vēl 21 dienu klāt */
public final class ItemEffects {

    /** Elements nosedz stādījumu, ja pārklāj vismaz pusi tā laukuma */
    static final double MIN_COVER_SHARE = 0.5;
    private static final DateTimeFormatter MM_DD = DateTimeFormatter.ofPattern("MM-dd");

    private ItemEffects() {
    }

    public record PlacedItem(long id, ItemType type, Geometry geometry) {
    }

    /** Ja affecting ir tukšs, nekas nemainās */
    public record Adjusted(List<PlacedItem> affecting, String outdoorFrom, String outdoorTo,
                           int harvestExtraDays, double frostProtectionC) {
    }

    /** Punkta elementu, piem. zvanu vai podu, uzskata par apli ar diametru default_w_m */
    public static List<PlacedItem> covering(Geometry target, List<PlacedItem> items) {
        List<PlacedItem> result = new ArrayList<>();
        for (PlacedItem item : items) {
            if (!item.type().affectsSeason()) {
                continue;
            }
            Geometry cover = footprint(item);
            if (cover == null || cover.isEmpty()) {
                continue;
            }
            boolean covers;
            if (target instanceof Point || target.getArea() <= 0) {
                covers = cover.covers(target);
            } else {
                covers = cover.intersection(target).getArea() >= MIN_COVER_SHARE * target.getArea() - 1e-9;
            }
            if (covers) {
                result.add(item);
            }
        }
        return result;
    }

    static Geometry footprint(PlacedItem item) {
        Geometry g = item.geometry();
        if (g instanceof Point) {
            double d = item.type().defaultWM() == null ? 0.4 : item.type().defaultWM();
            return g.buffer(d / 2, 16);
        }
        return g.getArea() > 0 ? g : null;
    }

    /** Āra logs sākas agrāk un beidzas vēlāk, bet paliek tajā pašā gadā, telpu sēja nemainās */
    public static Adjusted adjust(String outdoorFrom, String outdoorTo, int year, List<PlacedItem> affecting) {
        int start = 0;
        int end = 0;
        int harvest = 0;
        double frost = 0;
        for (PlacedItem item : affecting) {
            start = Math.max(start, item.type().startShiftDays());
            end = Math.max(end, item.type().endShiftDays());
            harvest = Math.max(harvest, item.type().harvestShiftDays());
            frost = Math.max(frost, item.type().frostProtectionC());
        }
        LocalDate from = MonthDay.parse("--" + outdoorFrom).atYear(year).minusDays(start);
        LocalDate to = MonthDay.parse("--" + outdoorTo).atYear(year).plusDays(end);
        LocalDate jan1 = LocalDate.of(year, 1, 1);
        LocalDate dec31 = LocalDate.of(year, 12, 31);
        if (from.isBefore(jan1)) {
            from = jan1;
        }
        if (to.isAfter(dec31)) {
            to = dec31;
        }
        return new Adjusted(List.copyOf(affecting), from.format(MM_DD), to.format(MM_DD), harvest, frost);
    }
}
