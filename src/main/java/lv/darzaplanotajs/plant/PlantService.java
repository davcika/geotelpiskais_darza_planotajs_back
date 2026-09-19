package lv.darzaplanotajs.plant;

import java.time.Instant;
import java.util.List;
import lv.darzaplanotajs.common.ApiErrors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Lietotāja paša augi, apstiprinātus īpašnieks vairs nemaina */
@Service
public class PlantService {

    private final PlantRepository plants;

    public PlantService(PlantRepository plants) {
        this.plants = plants;
    }

    public List<PlantResponse> listVisible(long userId) {
        return plants.findVisibleTo(userId).stream().map(p -> PlantResponse.from(p, userId)).toList();
    }

    @Transactional
    public PlantResponse create(long userId, PlantRequest request) {
        validate(request);
        Plant plant = new Plant(userId);
        plant.apply(request);
        setSharing(plant, request.shareWithEveryone());
        return PlantResponse.from(save(plant), userId);
    }

    @Transactional
    public PlantResponse update(long userId, long id, PlantRequest request) {
        validate(request);
        Plant plant = ownEditable(userId, id);
        plant.apply(request);
        plant.reviewNote = null;
        setSharing(plant, request.shareWithEveryone());
        return PlantResponse.from(save(plant), userId);
    }

    @Transactional
    public void delete(long userId, long id) {
        Plant plant = ownEditable(userId, id);
        try {
            plants.delete(plant);
            plants.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plant is used in plantings; remove them first", e);
        }
    }

    private static void setSharing(Plant plant, boolean share) {
        plant.status = (share ? PlantStatus.PENDING : PlantStatus.PRIVATE).name();
        plant.submittedAt = share ? Instant.now() : null;
    }

    private Plant save(Plant plant) {
        try {
            return plants.saveAndFlush(plant);
        } catch (DataIntegrityViolationException e) {
            // DB CHECK ir pēdējā aizsardzība, ja validate kaut ko palaida garām
            throw ApiErrors.badRequest("Plant data is not valid");
        }
    }

    /** Svešs vai iebūvēts augs dod 404, apstiprināts 409 */
    private Plant ownEditable(long userId, long id) {
        Plant plant = plants.findById(id)
                .filter(p -> p.ownerId != null && p.ownerId == userId)
                .orElseThrow(() -> ApiErrors.notFound("Plant", id));
        if (plant.statusEnum() == PlantStatus.APPROVED) {
            throw ApiErrors.conflict("Approved plants can no longer be changed");
        }
        return plant;
    }

    /** Tās pašas atkarības kā DB CHECK */
    static void validate(PlantRequest r) {
        boolean indoor = notBlank(r.indoorSowFrom()) && notBlank(r.indoorSowTo());
        if ("TRANSPLANT".equals(r.sowMethod()) != indoor) {
            throw ApiErrors.badRequest("Indoor sowing window is required for TRANSPLANT and only for it");
        }
        if ((r.daysToHarvest() == null) != (r.harvestWindowDays() == null)) {
            throw ApiErrors.badRequest("Set both days to harvest and harvest window, or neither");
        }
        if (notBlank(r.bloomFrom()) != notBlank(r.bloomTo())) {
            throw ApiErrors.badRequest("Set both bloom from and bloom to, or neither");
        }
        if (!r.evergreen() && notBlank(r.foliageFrom()) != notBlank(r.foliageTo())) {
            throw ApiErrors.badRequest("Set both foliage from and foliage to, or neither");
        }
        if (r.phMin() != null && r.phMax() != null && r.phMin() > r.phMax()) {
            throw ApiErrors.badRequest("pH min must not exceed pH max");
        }
        for (String date : new String[]{r.indoorSowFrom(), r.indoorSowTo(), r.outdoorFrom(), r.outdoorTo(),
                r.foliageFrom(), r.foliageTo(), r.bloomFrom(), r.bloomTo()}) {
            if (notBlank(date) && !isMonthDay(date)) {
                throw ApiErrors.badRequest("Invalid date " + date + " (expected MM-DD)");
            }
        }
    }

    private static boolean isMonthDay(String mmdd) {
        try {
            java.time.MonthDay.parse("--" + mmdd);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
