package lv.darzaplanotajs.soil;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoilProfileRepository extends JpaRepository<SoilProfile, Long> {

    Optional<SoilProfile> findByGardenIdAndObjectIdIsNull(long gardenId);

    Optional<SoilProfile> findByObjectId(long objectId);

    List<SoilProfile> findByGardenId(long gardenId);
}
