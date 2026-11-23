package lv.darzaplanotajs.garden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FrostCalculatorTest {

    /** Silts gads ar salnām tikai norādītajās dienās */
    private static void year(List<LocalDate> dates, List<Double> tmin, int year, LocalDate... frosts) {
        for (LocalDate d = LocalDate.of(year, 1, 1); d.getYear() == year; d = d.plusDays(1)) {
            dates.add(d);
            tmin.add(List.of(frosts).contains(d) ? -1.0 : 5.0);
        }
    }

    @Test
    void medianAndRangeOverYears() {
        List<LocalDate> dates = new ArrayList<>();
        List<Double> tmin = new ArrayList<>();
        year(dates, tmin, 2021, LocalDate.of(2021, 3, 1), LocalDate.of(2021, 5, 10), LocalDate.of(2021, 9, 20), LocalDate.of(2021, 11, 1));
        year(dates, tmin, 2022, LocalDate.of(2022, 5, 18), LocalDate.of(2022, 9, 27));
        year(dates, tmin, 2023, LocalDate.of(2023, 5, 30), LocalDate.of(2023, 10, 5));

        FrostCalculator.Estimate e = FrostCalculator.compute(dates, tmin);

        assertEquals("05-18", e.last());
        assertEquals("05-10", e.lastEarliest());
        assertEquals("05-30", e.lastLatest());
        assertEquals("09-27", e.first());
        assertEquals("09-20", e.firstEarliest());
        assertEquals("10-05", e.firstLatest());
        assertEquals(3, e.years());
    }

    @Test
    void zeroCountsAsFrostAndNullsAreIgnored() {
        List<LocalDate> dates = List.of(LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 2),
                LocalDate.of(2020, 10, 1), LocalDate.of(2020, 10, 2));
        List<Double> tmin = new ArrayList<>(List.of(0.0, 0.0, 0.0, 0.0));
        tmin.set(1, null);
        FrostCalculator.Estimate e = FrostCalculator.compute(dates, tmin);
        assertEquals("05-01", e.last());
        assertEquals("10-01", e.first());
    }

    @Test
    void evenYearCountAveragesMiddleDays() {
        List<LocalDate> dates = new ArrayList<>();
        List<Double> tmin = new ArrayList<>();
        year(dates, tmin, 2022, LocalDate.of(2022, 5, 10), LocalDate.of(2022, 9, 20));
        year(dates, tmin, 2024, LocalDate.of(2024, 5, 20), LocalDate.of(2024, 9, 30)); // garais gads
        FrostCalculator.Estimate e = FrostCalculator.compute(dates, tmin);
        assertEquals("05-15", e.last());
        assertEquals("09-25", e.first());
    }

    @Test
    void noFrostGivesNull() {
        List<LocalDate> dates = new ArrayList<>();
        List<Double> tmin = new ArrayList<>();
        year(dates, tmin, 2022);
        assertNull(FrostCalculator.compute(dates, tmin));
    }
}
