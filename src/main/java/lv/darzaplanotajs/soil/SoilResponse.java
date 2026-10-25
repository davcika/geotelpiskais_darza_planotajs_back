package lv.darzaplanotajs.soil;

import java.time.LocalDate;

public record SoilResponse(String soilType, Double ph, Double organicMatterPct, Double nitrogen, Double phosphorus,
                           Double potassium, String moisture, String light, LocalDate testedOn, String labNote) {

    static SoilResponse from(SoilProfile s) {
        return s == null ? null : new SoilResponse(s.soilType, s.ph, s.organicMatterPct, s.nitrogen, s.phosphorus,
                s.potassium, s.moisture, s.light, s.testedOn, s.labNote);
    }

    /** Īss augsnes apraksts modelim */
    public String describe() {
        StringBuilder sb = new StringBuilder(soilType == null ? "type unknown" : soilType);
        if (ph != null) sb.append(", pH ").append(ph);
        if (organicMatterPct != null) sb.append(", organic ").append(organicMatterPct).append("%");
        if (nitrogen != null || phosphorus != null || potassium != null) {
            sb.append(", N/P/K ").append(n(nitrogen)).append("/").append(n(phosphorus)).append("/").append(n(potassium))
                    .append(" mg/kg");
        }
        if (moisture != null) sb.append(", ").append(moisture);
        if (light != null) sb.append(", ").append(light);
        if (testedOn != null) sb.append(", tested ").append(testedOn);
        return sb.toString();
    }

    private static String n(Double v) {
        return v == null ? "?" : String.valueOf(v);
    }
}
