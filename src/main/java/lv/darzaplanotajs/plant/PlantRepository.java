package lv.darzaplanotajs.plant;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlantRepository extends JpaRepository<Plant, Long> {

    /** Tikai apstiprinātais katalogs, lieto arī AssistantService */
    @Query("SELECT p FROM Plant p WHERE p.status = 'APPROVED' ORDER BY p.nameEn")
    List<Plant> findAllByOrderByNameEn();

    /** Kopīgā dārza dalībnieku privātos augus redz visi, citādi viņu stādījumi nebūtu redzami */
    String CO_USERS = """
            (SELECT g.owner_id FROM garden g JOIN garden_member m ON m.garden_id = g.id WHERE m.user_id = :userId
             UNION SELECT m.user_id FROM garden_member m JOIN garden g ON g.id = m.garden_id WHERE g.owner_id = :userId
             UNION SELECT o.user_id FROM garden_member m JOIN garden_member o ON o.garden_id = m.garden_id
                   WHERE m.user_id = :userId)
            """;

    /** Kopīgais katalogs, paša augi un kopīgo dārzu dalībnieku augi */
    @Query(value = "SELECT p.* FROM plant p WHERE p.status = 'APPROVED' OR p.owner_id = :userId OR p.owner_id IN "
            + CO_USERS + " ORDER BY p.name_en", nativeQuery = true)
    List<Plant> findVisibleTo(@Param("userId") Long userId);

    List<Plant> findByStatusOrderBySubmittedAt(String status);

    /** Cits svešs augs izskatās tā, it kā tāda nebūtu */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM plant p WHERE p.id = :plantId AND (p.status = 'APPROVED'"
            + " OR p.owner_id = :userId OR p.owner_id IN " + CO_USERS + "))", nativeQuery = true)
    boolean isUsableBy(@Param("plantId") Long plantId, @Param("userId") Long userId);

    @Query("SELECT count(p) > 0 FROM Plant p WHERE p.status = 'APPROVED' AND lower(p.nameEn) = lower(:nameEn)")
    boolean existsApprovedName(String nameEn);
}
