package lv.darzaplanotajs.planting;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import lv.darzaplanotajs.common.ApiErrors;
import lv.darzaplanotajs.garden.BedFrame;
import lv.darzaplanotajs.garden.BedGrid;
import org.locationtech.jts.geom.Geometry;
import lv.darzaplanotajs.garden.GardenObject;
import lv.darzaplanotajs.garden.GardenService;
import lv.darzaplanotajs.garden.ObjectType;
import lv.darzaplanotajs.plant.PlantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Piederību pārbauda caur dobi, svešam stādījumam 404 */
@Service
@Transactional
public class PlantingService {

    private final PlantingRepository plantings;
    private final PlantRepository plants;
    private final GardenService gardens;

    public PlantingService(PlantingRepository plantings, PlantRepository plants, GardenService gardens) {
        this.plantings = plantings;
        this.plants = plants;
        this.gardens = gardens;
    }

    @Transactional(readOnly = true)
    public List<PlantingResponse> findForGarden(long userId, long gardenId, int year) {
        gardens.findOwnGarden(userId, gardenId);
        return plantings.findByGardenAndYear(gardenId, year).stream().map(PlantingResponse::from).toList();
    }

    public PlantingResponse create(long userId, long objectId, PlantingRequest request) {
        GardenObject object = gardens.findOwnObject(userId, objectId);
        if (object.getType() != ObjectType.BED) {
            throw ApiErrors.badRequest("Plantings can only be added to beds");
        }
        Planting planting = new Planting(object.getId());
        apply(userId, planting, object, request);
        return PlantingResponse.from(plantings.save(planting));
    }

    public PlantingResponse update(long userId, long id, PlantingRequest request) {
        Planting planting = findOwn(userId, id);
        apply(userId, planting, gardens.findOwnObject(userId, planting.objectId), request);
        return PlantingResponse.from(planting);
    }

    public void delete(long userId, long id) {
        plantings.delete(findOwn(userId, id));
    }

    private void apply(long userId, Planting planting, GardenObject bed, PlantingRequest request) {
        if (!plants.isUsableBy(request.plantId(), userId)) {
            throw ApiErrors.badRequest("Unknown plant " + request.plantId());
        }
        if (request.cells() != null && !request.cells().isEmpty()) {
            requireFreeCellsInBed(planting, bed, request);
        }
        if (request.block() != null) {
            requireFreeBlockInBed(planting, bed, request);
        }
        planting.update(request);
    }

    /** Šūnām jābūt dobē un tajā sezonā neaizņemtām ar citu stādījumu */
    private void requireFreeCellsInBed(Planting planting, GardenObject bed, PlantingRequest request) {
        BedGrid grid = new BedGrid(bed);
        if (!request.cells().stream().allMatch(cell -> grid.fits(cell.get(0), cell.get(1)))) {
            throw ApiErrors.badRequest("Some cells are outside the bed");
        }
        Set<List<Integer>> taken = new HashSet<>();
        othersInBed(planting, bed, request)
                .forEach(other -> taken.addAll(other.cellList()));
        if (request.cells().stream().anyMatch(taken::contains)) {
            throw ApiErrors.badRequest("Some cells are already used by another planting this season");
        }
    }

    /** Blokam jābūt dobes poligonā un tajā sezonā nepārklāties ar citu stādījumu bloku */
    private void requireFreeBlockInBed(Planting planting, GardenObject bed, PlantingRequest request) {
        BlockRules.Block block = request.block();
        Geometry geometry = BlockRules.parseBed(bed.getGeometry());
        BedFrame frame = new BedFrame(geometry, request.blockAngle() == null ? 0 : request.blockAngle());
        if (!BlockRules.insideBed(frame, geometry, block)) {
            throw ApiErrors.badRequest("The block is outside the bed");
        }
        List<BlockRules.Block> others = othersInBed(planting, bed, request).stream().filter(p -> p.block() != null)
                .map(p -> BlockRules.inFrame(p.block(), p.blockAngle, frame, geometry)).toList();
        if (BlockRules.overlapsAny(block, others)) {
            throw ApiErrors.badRequest("The block overlaps another planting this season");
        }
    }

    /** Citi tās pašas sezonas stādījumi dobē (izņemot pašu, ja tas jau saglabāts) */
    private List<Planting> othersInBed(Planting planting, GardenObject bed, PlantingRequest request) {
        long exceptId = planting.id == null ? -1 : planting.id;
        return plantings.findOthersInBed(bed.getId(), request.sownOn().getYear(), exceptId);
    }

    private Planting findOwn(long userId, long id) {
        Planting planting = plantings.findById(id)
                .orElseThrow(() -> ApiErrors.notFound("Planting", id));
        gardens.findOwnObject(userId, planting.objectId); // svešai dobei met 404
        return planting;
    }
}
