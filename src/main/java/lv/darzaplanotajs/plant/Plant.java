package lv.darzaplanotajs.plant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

/**
 * Datumi glabājas kā mēnesis un diena.
 * Iebūvētos un apstiprinātos augus lietotne nemaina, rakstīt drīkst tikai caur PlantService.
 */
@Entity
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nameEn;
    String nameLv;
    String family;
    String category;
    String sowMethod;
    String indoorSowFrom;
    String indoorSowTo;
    String outdoorFrom;
    String outdoorTo;
    Integer daysToHarvest;
    Integer harvestWindowDays;
    boolean frostSensitive;
    boolean perennial;
    boolean evergreen;
    String foliageFrom;
    String foliageTo;
    String bloomFrom;
    String bloomTo;
    String bloomColor;
    // Beigu lielais burts netiek pārvērsts par _m, tāpēc kolonnas nosaukums dots tieši
    @Column(name = "height_m")
    Double heightM;
    @Column(name = "spread_m")
    Double spreadM;
    Integer yearsToMature;
    Double phMin;
    Double phMax;
    String light;
    /** Atstatums rindā un starp rindām centimetros */
    Integer spacingCm;
    Integer rowSpacingCm;
    /** Emocijzīme vai ikonas atslēga */
    String icon;
    /** null nozīmē iebūvētu augu */
    Long ownerId;
    /** PlantStatus nosaukums */
    String status;
    Instant submittedAt;
    String reviewNote;

    protected Plant() {
    }

    Plant(Long ownerId) {
        this.ownerId = ownerId;
    }

    PlantStatus statusEnum() {
        return PlantStatus.valueOf(status);
    }

    /** Statusu te neaiztiek, to nosaka PlantService */
    void apply(PlantRequest r) {
        nameEn = r.nameEn().trim();
        nameLv = r.nameLv().trim();
        family = r.family().trim().toUpperCase(java.util.Locale.ROOT);
        category = r.category();
        sowMethod = r.sowMethod();
        indoorSowFrom = blankToNull(r.indoorSowFrom());
        indoorSowTo = blankToNull(r.indoorSowTo());
        outdoorFrom = r.outdoorFrom();
        outdoorTo = r.outdoorTo();
        daysToHarvest = r.daysToHarvest();
        harvestWindowDays = r.harvestWindowDays();
        frostSensitive = r.frostSensitive();
        perennial = r.perennial();
        evergreen = r.evergreen();
        foliageFrom = blankToNull(r.foliageFrom());
        foliageTo = blankToNull(r.foliageTo());
        bloomFrom = blankToNull(r.bloomFrom());
        bloomTo = blankToNull(r.bloomTo());
        bloomColor = blankToNull(r.bloomColor());
        heightM = r.heightM();
        spreadM = r.spreadM();
        yearsToMature = r.yearsToMature();
        phMin = r.phMin();
        phMax = r.phMax();
        light = blankToNull(r.light());
        if (r.spacingCm() != null) spacingCm = r.spacingCm();
        if (r.rowSpacingCm() != null) rowSpacingCm = r.rowSpacingCm();
        if (r.icon() != null && !r.icon().isBlank()) icon = r.icon().trim();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
