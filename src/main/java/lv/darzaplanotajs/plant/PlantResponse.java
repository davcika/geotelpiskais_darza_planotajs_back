package lv.darzaplanotajs.plant;

/** Izmēri ir metros pieaugušam augam, un mine nozīmē, ka augs ir paša lietotāja */
public record PlantResponse(Long id, String nameEn, String nameLv, String family, String category, String sowMethod,
                            String indoorSowFrom, String indoorSowTo, String outdoorFrom, String outdoorTo,
                            Integer daysToHarvest, Integer harvestWindowDays, boolean frostSensitive,
                            boolean perennial, boolean evergreen, String foliageFrom, String foliageTo,
                            String bloomFrom, String bloomTo, String bloomColor,
                            Double heightM, Double spreadM, Integer yearsToMature,
                            Double phMin, Double phMax, String light,
                            String status, boolean mine, String reviewNote,
                            Integer spacingCm, Integer rowSpacingCm, String icon) {

    public static PlantResponse from(Plant p) {
        return from(p, null);
    }

    public static PlantResponse from(Plant p, Long userId) {
        return new PlantResponse(p.id, p.nameEn, p.nameLv, p.family, p.category, p.sowMethod,
                p.indoorSowFrom, p.indoorSowTo, p.outdoorFrom, p.outdoorTo,
                p.daysToHarvest, p.harvestWindowDays, p.frostSensitive,
                p.perennial, p.evergreen, p.foliageFrom, p.foliageTo,
                p.bloomFrom, p.bloomTo, p.bloomColor,
                p.heightM, p.spreadM, p.yearsToMature,
                p.phMin, p.phMax, p.light,
                p.status, userId != null && userId.equals(p.ownerId), p.reviewNote,
                p.spacingCm, p.rowSpacingCm, p.icon);
    }
}
