package lv.darzaplanotajs.item;

import java.util.List;

/** Kuri elementi ietekmē stādījumu un kā mainās tā logi */
public record ItemEffectResponse(List<AffectingItem> affectedBy, String outdoorFrom, String outdoorTo,
                                 int harvestExtraDays, double frostProtectionC) {

    public record AffectingItem(long itemId, String name, String icon, String typeKey) {
    }
}
