package lv.darzaplanotajs.garden;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import lv.darzaplanotajs.common.ApiErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Salnas rēķina no 10 gadu laika vēstures, un ja neizdodas, ņem Latvijas noklusējumu.
 * HTTP izsaukums ir ārpus transakcijas, lai velti neturētu DB savienojumu.
 */
@Service
public class FrostService {

    static final String DEFAULT_LAST = "05-20";
    static final String DEFAULT_FIRST = "09-25";
    static final int YEARS = 10;
    /** Ap 1 km, tuvākam punktam der saglabātais rezultāts */
    private static final double SAME_POINT_DEG = 0.01;

    private static final Logger log = LoggerFactory.getLogger(FrostService.class);

    private final GardenService gardens;
    private final OpenMeteoClient openMeteo;

    public FrostService(GardenService gardens, OpenMeteoClient openMeteo) {
        this.gardens = gardens;
        this.openMeteo = openMeteo;
    }

    public FrostResponse compute(long userId, long gardenId, FrostComputeRequest request) {
        Garden garden = gardens.findOwnGarden(userId, gardenId);
        if ("OPEN_METEO".equals(garden.getFrostSource()) && garden.getFrostLat() != null
                && Math.abs(garden.getFrostLat() - request.lat()) < SAME_POINT_DEG
                && Math.abs(garden.getFrostLon() - request.lon()) < SAME_POINT_DEG) {
            return FrostResponse.from(garden, YEARS);
        }
        int lastYear = LocalDate.now(ZoneId.of("Europe/Riga")).getYear() - 1;
        FrostCalculator.Estimate estimate = null;
        try {
            OpenMeteoClient.DailySeries series = openMeteo.dailyMin(request.lat(), request.lon(),
                    LocalDate.of(lastYear - YEARS + 1, 1, 1), LocalDate.of(lastYear, 12, 31));
            estimate = FrostCalculator.compute(series.dates(), series.tmin());
        } catch (RuntimeException e) {
            log.warn("Open-Meteo frost lookup failed, using defaults: {}", e.toString());
        }
        Garden saved = estimate == null
                ? gardens.applyFrost(userId, gardenId, "DEFAULT", DEFAULT_LAST, DEFAULT_FIRST, null, request.lat(), request.lon())
                : gardens.applyFrost(userId, gardenId, "OPEN_METEO", estimate.last(), estimate.first(), estimate,
                        request.lat(), request.lon());
        return FrostResponse.from(saved, estimate == null ? null : estimate.years());
    }

    public FrostResponse setManual(long userId, long gardenId, FrostRequest request) {
        try {
            MonthDay last = FrostCalculator.parse(request.lastFrost());
            MonthDay first = FrostCalculator.parse(request.firstFrost());
            if (!last.isBefore(first)) {
                throw ApiErrors.badRequest("Last spring frost must be before first autumn frost");
            }
        } catch (DateTimeParseException e) {
            throw ApiErrors.badRequest("Dates must be valid MM-DD");
        }
        return FrostResponse.from(gardens.applyFrost(userId, gardenId, "MANUAL", request.lastFrost(),
                request.firstFrost(), null, null, null), null);
    }
}
