package lv.darzaplanotajs.garden;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Vienīgā klase, kas zina par Open Meteo, kļūdas gadījumā met izņēmumu */
@Component
public class OpenMeteoClient {

    public static final String ATTRIBUTION = "Weather data by Open-Meteo.com (CC BY 4.0)";

    private final RestClient http;

    public OpenMeteoClient() {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(20));
        this.http = RestClient.builder().baseUrl("https://archive-api.open-meteo.com").requestFactory(factory).build();
    }

    /** Trūkstošas vērtības ir null */
    public DailySeries dailyMin(double lat, double lon, LocalDate from, LocalDate to) {
        ArchiveResponse response = http.get()
                .uri(uri -> uri.path("/v1/archive")
                        .queryParam("latitude", String.format(Locale.ROOT, "%.4f", lat))
                        .queryParam("longitude", String.format(Locale.ROOT, "%.4f", lon))
                        .queryParam("start_date", from)
                        .queryParam("end_date", to)
                        .queryParam("daily", "temperature_2m_min")
                        .queryParam("timezone", "Europe/Riga")
                        .build())
                .retrieve()
                .body(ArchiveResponse.class);
        if (response == null || response.daily() == null || response.daily().time() == null
                || response.daily().temperature_2m_min() == null) {
            throw new IllegalStateException("Open-Meteo returned no data");
        }
        List<LocalDate> dates = response.daily().time().stream().map(LocalDate::parse).toList();
        return new DailySeries(dates, response.daily().temperature_2m_min());
    }

    public record DailySeries(List<LocalDate> dates, List<Double> tmin) {
    }

    record ArchiveResponse(Daily daily) {
    }

    @SuppressWarnings("java:S116")
    record Daily(List<String> time, List<Double> temperature_2m_min) {
    }
}
