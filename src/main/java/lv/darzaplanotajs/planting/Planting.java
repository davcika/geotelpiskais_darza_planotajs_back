package lv.darzaplanotajs.planting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Sezona ir sownOn gads */
@Entity
public class Planting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "garden_object_id", nullable = false)
    Long objectId;

    @Column(nullable = false)
    Long plantId;

    @Column(nullable = false)
    LocalDate sownOn;

    String variety;
    String note;
    /** Šūnas kā kolonnas un rindas pāri vai null */
    String cells;
    /** Bloks metros dobes lokālajās koordinātās, null ja bloka nav */
    @Column(name = "block_x")
    Double blockX;
    @Column(name = "block_y")
    Double blockY;
    @Column(name = "block_w")
    Double blockW;
    @Column(name = "block_h")
    Double blockH;
    /** null nozīmē vecu bloku ar 0° */
    @Column(name = "block_angle")
    Double blockAngle;
    Integer plantCount;

    protected Planting() {
    }

    Planting(Long objectId) {
        this.objectId = objectId;
    }

    void update(PlantingRequest request) {
        this.plantId = request.plantId();
        this.sownOn = request.sownOn();
        this.variety = request.variety();
        this.note = request.note();
        this.cells = request.cells() == null || request.cells().isEmpty() ? null : String.join(";",
                request.cells().stream().map(cell -> cell.get(0) + "," + cell.get(1)).toList());
        this.blockX = request.blockX();
        this.blockY = request.blockY();
        this.blockW = request.blockW();
        this.blockH = request.blockH();
        this.blockAngle = request.block() == null ? null : request.blockAngle();
        this.plantCount = request.plantCount();
    }

    BlockRules.Block block() {
        return blockX == null ? null : new BlockRules.Block(blockX, blockY, blockW, blockH);
    }

    List<List<Integer>> cellList() {
        if (cells == null) return List.of();
        return Arrays.stream(cells.split(";"))
                .map(cell -> Arrays.stream(cell.split(",")).map(Integer::valueOf).toList())
                .toList();
    }
}
