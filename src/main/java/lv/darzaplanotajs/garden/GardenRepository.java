package lv.darzaplanotajs.garden;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GardenRepository extends JpaRepository<Garden, Long> {

    List<Garden> findByOwnerIdOrderById(Long ownerId);

    Optional<Garden> findByIdAndOwnerId(Long id, Long ownerId);

    /** Savējie dārzi un tie, kuros lietotājs ir redaktors */
    @Query(value = """
            SELECT g.* FROM garden g
            WHERE g.owner_id = :userId
               OR EXISTS (SELECT 1 FROM garden_member m WHERE m.garden_id = g.id AND m.user_id = :userId)
            ORDER BY g.id
            """, nativeQuery = true)
    List<Garden> findAccessible(@Param("userId") Long userId);

    @Query(value = """
            SELECT g.* FROM garden g
            WHERE g.id = :id AND (g.owner_id = :userId
               OR EXISTS (SELECT 1 FROM garden_member m WHERE m.garden_id = g.id AND m.user_id = :userId))
            """, nativeQuery = true)
    Optional<Garden> findAccessible(@Param("id") Long id, @Param("userId") Long userId);
}
