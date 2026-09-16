package lv.darzaplanotajs.garden;

/** estimated nozīmē, ka tas ir Latvijas noklusējums */
public record FrostResponse(String lastFrost, String firstFrost,
                            String lastFrostEarliest, String lastFrostLatest,
                            String firstFrostEarliest, String firstFrostLatest,
                            String source, Integer years, boolean estimated, String attribution) {

    static FrostResponse from(Garden g, Integer years) {
        return new FrostResponse(g.getLastFrost(), g.getFirstFrost(), g.getLastFrostEarliest(), g.getLastFrostLatest(),
                g.getFirstFrostEarliest(), g.getFirstFrostLatest(), g.getFrostSource(), years,
                "DEFAULT".equals(g.getFrostSource()),
                "OPEN_METEO".equals(g.getFrostSource()) ? OpenMeteoClient.ATTRIBUTION : null);
    }
}
