package lv.darzaplanotajs.garden;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GardenObjectRepository extends JpaRepository<GardenObject, Long> {

    List<GardenObject> findByGardenIdOrderById(Long gardenId);

    /** Vai kādam dobes stādījumam (jebkurā sezonā) ir iekrāsotas šūnas */
    @Query(value = "select exists (select 1 from planting where garden_object_id = :id and cells is not null)",
            nativeQuery = true)
    boolean hasPaintedCells(long id);

    /** Vai augs ir katalogā un der kokam (koks, krūms vai dzīvžogs) */
    @Query(value = "select exists (select 1 from plant where id = :plantId and category in ('TREE', 'SHRUB', 'HEDGE'))",
            nativeQuery = true)
    boolean isWoodyPlant(long plantId);
}
