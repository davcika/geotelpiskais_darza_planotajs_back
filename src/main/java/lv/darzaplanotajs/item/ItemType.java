package lv.darzaplanotajs.item;

/** No tabulas item_type, tikai lasāms */
public record ItemType(String key, String nameEn, String nameLv, String icon, String shape, String category,
                       Double defaultWM, Double defaultHM, int startShiftDays, int endShiftDays,
                       int harvestShiftDays, double frostProtectionC, boolean climbingSupport) {

    /** Vai elements maina sezonas logus (aizsardzības elementi ar nobīdi) */
    public boolean affectsSeason() {
        return startShiftDays > 0 || endShiftDays > 0 || harvestShiftDays > 0 || frostProtectionC > 0;
    }
}
