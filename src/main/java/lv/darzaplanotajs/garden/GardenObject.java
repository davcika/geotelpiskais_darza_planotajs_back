package lv.darzaplanotajs.garden;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Kartē iezīmēts objekts, piem. dobe vai koks */
@Entity
public class GardenObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gardenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjectType type;

    @Column(nullable = false)
    private String name;

    /** GeoJSON EPSG 3059 koordinātās */
    @Column(nullable = false)
    private String geometry;

    @Column(name = "crown_radius_m")
    private Double crownRadiusM;

    /** null nozīmē noklusējumu no BedGrid */
    private Integer gridCols;
    private Integer gridRows;

    /** Kokam augs un stādīšanas gads, abi nav obligāti */
    private Long plantId;
    private Integer plantedYear;

    protected GardenObject() {
    }

    public GardenObject(Long gardenId) {
        this.gardenId = gardenId;
    }

    public void update(GardenObjectRequest request) {
        this.type = request.type();
        this.name = request.name();
        this.geometry = request.geometry();
        this.crownRadiusM = request.crownRadiusM();
        this.gridCols = request.gridCols();
        this.gridRows = request.gridRows();
        this.plantId = request.plantId();
        this.plantedYear = request.plantedYear();
    }

    public Long getId() {
        return id;
    }

    public Long getGardenId() {
        return gardenId;
    }

    public ObjectType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getGeometry() {
        return geometry;
    }

    public Double getCrownRadiusM() {
        return crownRadiusM;
    }

    public Integer getGridCols() {
        return gridCols;
    }

    public Integer getGridRows() {
        return gridRows;
    }

    public Long getPlantId() {
        return plantId;
    }

    public Integer getPlantedYear() {
        return plantedYear;
    }
}
