package lv.darzaplanotajs.garden;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Garden {

    /** Jāsakrīt ar V16 CHECK un frontend sarakstu */
    public static final String ICON_PATTERN = "^(leaf|carrot|flower|tree|apple|sun|house|drop)$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String name;

    /** null, ja lietotājs adresi izlaida */
    private Long addressCode;

    private String addressLabel;

    /** null līdz onboarding beigām */
    private String hubGeometry;

    @Column(nullable = false)
    private String icon = "leaf";

    @Column(nullable = false)
    private String color = "#2d6a3d";

    /** Mediāna un diapazons pa gadiem */
    private String lastFrost;
    private String firstFrost;
    private String lastFrostEarliest;
    private String lastFrostLatest;
    private String firstFrostEarliest;
    private String firstFrostLatest;
    /** OPEN_METEO, MANUAL vai DEFAULT */
    private String frostSource;
    private Instant frostUpdatedAt;
    /** Punkts, kuram salnas aprēķinātas, lai zinātu, kad kešs vēl der */
    private Double frostLat;
    private Double frostLon;

    protected Garden() {
    }

    public Garden(Long ownerId, String name, String icon, String color) {
        this.ownerId = ownerId;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public void setHub(Long addressCode, String addressLabel, String hubGeometry) {
        this.addressCode = addressCode;
        this.addressLabel = addressLabel;
        this.hubGeometry = hubGeometry;
    }

    public void rename(String name, String icon, String color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    /** Manuāli ievadītiem datumiem diapazons un punkts ir null */
    public void setFrost(String source, String last, String first, FrostCalculator.Estimate range, Double lat, Double lon) {
        this.frostSource = source;
        this.lastFrost = last;
        this.firstFrost = first;
        this.lastFrostEarliest = range == null ? null : range.lastEarliest();
        this.lastFrostLatest = range == null ? null : range.lastLatest();
        this.firstFrostEarliest = range == null ? null : range.firstEarliest();
        this.firstFrostLatest = range == null ? null : range.firstLatest();
        this.frostLat = lat;
        this.frostLon = lon;
        this.frostUpdatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Long getAddressCode() {
        return addressCode;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public String getHubGeometry() {
        return hubGeometry;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public String getLastFrost() {
        return lastFrost;
    }

    public String getFirstFrost() {
        return firstFrost;
    }

    public String getLastFrostEarliest() {
        return lastFrostEarliest;
    }

    public String getLastFrostLatest() {
        return lastFrostLatest;
    }

    public String getFirstFrostEarliest() {
        return firstFrostEarliest;
    }

    public String getFirstFrostLatest() {
        return firstFrostLatest;
    }

    public String getFrostSource() {
        return frostSource;
    }

    public Instant getFrostUpdatedAt() {
        return frostUpdatedAt;
    }

    public Double getFrostLat() {
        return frostLat;
    }

    public Double getFrostLon() {
        return frostLon;
    }
}
