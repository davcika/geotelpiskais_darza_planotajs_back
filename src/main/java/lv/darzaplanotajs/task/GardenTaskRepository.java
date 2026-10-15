package lv.darzaplanotajs.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GardenTaskRepository extends JpaRepository<GardenTask, Long> {

    List<GardenTask> findByGardenIdOrderByIdAsc(long gardenId);

    boolean existsByGardenIdAndIsDefaultTrueAndTitle(long gardenId, String title);
}
