package lv.darzaplanotajs.planting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlantingRepository extends JpaRepository<Planting, Long> {

    /** Visi dārza stādījumi vienā sezonā (kartes uzrakstiem un panelim) */
    @Query("""
            select p from Planting p
            where p.objectId in (select o.id from GardenObject o where o.gardenId = :gardenId)
              and year(p.sownOn) = :year
            order by p.sownOn, p.id""")
    List<Planting> findByGardenAndYear(long gardenId, int year);

    /** Citi tās pašas dobes stādījumi sezonā (šūnu pārklāšanās pārbaudei) */
    @Query("select p from Planting p where p.objectId = :objectId and year(p.sownOn) = :year and p.id <> :exceptId")
    List<Planting> findOthersInBed(long objectId, int year, long exceptId);
}
