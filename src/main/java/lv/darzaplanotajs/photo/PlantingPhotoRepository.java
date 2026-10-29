package lv.darzaplanotajs.photo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlantingPhotoRepository extends JpaRepository<PlantingPhoto, Long> {

    List<PlantingPhoto> findByPlantingIdOrderById(Long plantingId);

    long countByPlantingId(Long plantingId);

    boolean existsByStorageKey(String storageKey);

    @Query("select p.storageKey from PlantingPhoto p")
    List<String> findAllStorageKeys();
}
