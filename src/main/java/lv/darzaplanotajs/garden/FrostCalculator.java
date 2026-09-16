package lv.darzaplanotajs.garden;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** No dienas minimālajām temperatūrām atrod pēdējo pavasara un pirmo rudens salnu un ņem mediānu pa gadiem */
public final class FrostCalculator {

    /** years ir cik gadu dati izmantoti */
    public record Estimate(String last, String lastEarliest, String lastLatest,
                           String first, String firstEarliest, String firstLatest, int years) {
    }

    private FrostCalculator() {
    }

    /** Atgriež null, ja nevienā gadā nav abu salnu */
    public static Estimate compute(List<LocalDate> dates, List<Double> tmin) {
        Map<Integer, LocalDate> lastByYear = new TreeMap<>();
        Map<Integer, LocalDate> firstByYear = new TreeMap<>();
        for (int i = 0; i < dates.size() && i < tmin.size(); i++) {
            Double t = tmin.get(i);
            LocalDate d = dates.get(i);
            if (t == null || d == null || t > 0) {
                continue;
            }
            if (d.getMonthValue() <= 6) {
                lastByYear.merge(d.getYear(), d, (a, b) -> a.isAfter(b) ? a : b);
            } else {
                firstByYear.merge(d.getYear(), d, (a, b) -> a.isBefore(b) ? a : b);
            }
        }
        if (lastByYear.isEmpty() || firstByYear.isEmpty()) {
            return null;
        }
        List<Integer> last = dayNumbers(lastByYear);
        List<Integer> first = dayNumbers(firstByYear);
        int years = Math.min(last.size(), first.size());
        return new Estimate(format(median(last)), format(last.getFirst()), format(last.getLast()),
                format(median(first)), format(first.getFirst()), format(first.getLast()), years);
    }

    /** Dienas numurs negarajā gadā (2001), lai 29. februāris netraucētu salīdzināt */
    private static List<Integer> dayNumbers(Map<Integer, LocalDate> byYear) {
        List<Integer> days = new ArrayList<>();
        for (LocalDate d : byYear.values()) {
            int day = Math.min(d.getDayOfMonth(), d.getMonth().length(false));
            days.add(LocalDate.of(2001, d.getMonth(), day).getDayOfYear());
        }
        days.sort(null);
        return days;
    }

    private static int median(List<Integer> sorted) {
        int n = sorted.size();
        return n % 2 == 1 ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2;
    }

    private static String format(int dayOfYear) {
        LocalDate d = LocalDate.ofYearDay(2001, dayOfYear);
        return String.format("%02d-%02d", d.getMonthValue(), d.getDayOfMonth());
    }

    /** Nederīgam datumam, piem. 30. februārim, met izņēmumu */
    public static MonthDay parse(String mmdd) {
        return MonthDay.parse("--" + mmdd);
    }
}
