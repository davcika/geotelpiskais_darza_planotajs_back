package lv.darzaplanotajs.soil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

/** objectId null nozīmē dārza noklusējumu, citādi tā ir konkrētas dobes augsne */
@Entity
public class SoilProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long gardenId;

    Long objectId;

    String soilType;
    Double ph;
    Double organicMatterPct;
    Double nitrogen;
    Double phosphorus;
    Double potassium;
    String moisture;
    String light;
    LocalDate testedOn;
    String labNote;

    protected SoilProfile() {
    }

    SoilProfile(long gardenId, Long objectId) {
        this.gardenId = gardenId;
        this.objectId = objectId;
    }

    void update(SoilRequest r) {
        soilType = r.soilType();
        ph = r.ph();
        organicMatterPct = r.organicMatterPct();
        nitrogen = r.nitrogen();
        phosphorus = r.phosphorus();
        potassium = r.potassium();
        moisture = r.moisture();
        light = r.light();
        testedOn = r.testedOn();
        labNote = r.labNote() == null || r.labNote().isBlank() ? null : r.labNote().trim();
    }
}
