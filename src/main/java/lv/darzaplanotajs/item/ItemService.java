package lv.darzaplanotajs.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lv.darzaplanotajs.common.ApiErrors;
import lv.darzaplanotajs.garden.Garden;
import lv.darzaplanotajs.garden.GardenService;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemRepository repo;
    private final GardenService gardens;
    private final GeometryFactory factory = new GeometryFactory();

    public ItemService(ItemRepository repo, GardenService gardens) {
        this.repo = repo;
        this.gardens = gardens;
    }

    public List<ItemType> types() {
        return repo.types();
    }

    public List<GardenItemResponse> list(long userId, long gardenId) {
        gardens.findOwnGarden(userId, gardenId);
        Map<String, ItemType> types = typeMap();
        return repo.findByGarden(gardenId).stream().map(row -> toResponse(row, types.get(row.typeKey()))).toList();
    }

    @Transactional
    public GardenItemResponse create(long userId, long gardenId, GardenItemRequest request) {
        Garden garden = gardens.findOwnGarden(userId, gardenId);
        validate(garden, request);
        long id = repo.insert(gardenId, request);
        return get(id);
    }

    @Transactional
    public GardenItemResponse update(long userId, long id, GardenItemRequest request) {
        Garden garden = gardens.findOwnGarden(userId, ownRow(userId, id).gardenId());
        validate(garden, request);
        repo.update(id, request);
        return get(id);
    }

    @Transactional
    public void delete(long userId, long id) {
        ownRow(userId, id);
        repo.delete(id);
    }

    /** Stādījumi bez ietekmējošiem elementiem netiek iekļauti */
    public Map<Long, ItemEffectResponse> effects(long userId, long gardenId, int year) {
        gardens.findOwnGarden(userId, gardenId);
        Map<String, ItemType> types = typeMap();
        List<ItemEffects.PlacedItem> placed = new ArrayList<>();
        Map<Long, ItemRepository.ItemRow> rows = new LinkedHashMap<>();
        for (var row : repo.findByGarden(gardenId)) {
            ItemType type = types.get(row.typeKey());
            if (type != null && type.affectsSeason()) {
                placed.add(new ItemEffects.PlacedItem(row.id(), type, ItemGeometry.parse(row.geometry())));
                rows.put(row.id(), row);
            }
        }
        Map<Long, ItemEffectResponse> result = new LinkedHashMap<>();
        if (placed.isEmpty()) {
            return result;
        }
        for (var p : repo.plantings(gardenId, year)) {
            Geometry target = plantingArea(p);
            var covering = ItemEffects.covering(target, placed);
            if (covering.isEmpty()) {
                continue;
            }
            var adj = ItemEffects.adjust(p.outdoorFrom(), p.outdoorTo(), year, covering);
            var by = covering.stream().map(i -> new ItemEffectResponse.AffectingItem(i.id(),
                    rows.get(i.id()).label() != null ? rows.get(i.id()).label() : i.type().nameEn(),
                    i.type().icon(), rows.get(i.id()).typeKey())).toList();
            result.put(p.id(), new ItemEffectResponse(by, adj.outdoorFrom(), adj.outdoorTo(),
                    adj.harvestExtraDays(), adj.frostProtectionC()));
        }
        return result;
    }

    /** Bloks ir dobes lokālajās koordinātās, bez bloka ņem visu dobi */
    Geometry plantingArea(ItemRepository.PlantingRow p) {
        Geometry bed = ItemGeometry.parse(p.bedGeometry());
        if (p.blockX() == null) {
            return bed;
        }
        Envelope env = bed.getEnvelopeInternal();
        double x = env.getMinX() + p.blockX();
        double y = env.getMinY() + p.blockY();
        return factory.toGeometry(new Envelope(x, x + p.blockW(), y, y + p.blockH()));
    }

    private void validate(Garden garden, GardenItemRequest request) {
        ItemType type = repo.type(request.typeKey())
                .orElseThrow(() -> ApiErrors.badRequest("Unknown item type " + request.typeKey()));
        Geometry g = ItemGeometry.validFor(type.shape(), request.geometry());
        if (!ItemGeometry.insideZone(garden.getHubGeometry(), g)) {
            throw ApiErrors.badRequest("Item must be inside the garden zone");
        }
    }

    private ItemRepository.ItemRow ownRow(long userId, long id) {
        return repo.findById(id)
                .filter(row -> gardens.canAccess(userId, row.gardenId()))
                .orElseThrow(() -> ApiErrors.notFound("Item", id));
    }

    private GardenItemResponse get(long id) {
        var row = repo.findById(id).orElseThrow(() -> ApiErrors.notFound("Item", id));
        return toResponse(row, repo.type(row.typeKey()).orElse(null));
    }

    private Map<String, ItemType> typeMap() {
        return repo.types().stream().collect(Collectors.toMap(ItemType::key, Function.identity()));
    }

    static GardenItemResponse toResponse(ItemRepository.ItemRow row, ItemType type) {
        Double length = null;
        Double area = null;
        if (type != null) {
            Geometry g = ItemGeometry.parse(row.geometry());
            if ("LINE".equals(type.shape())) {
                length = round2(g.getLength());
            } else if ("AREA".equals(type.shape())) {
                area = round2(g.getArea());
            }
        }
        return new GardenItemResponse(row.id(), row.gardenId(), row.typeKey(), row.geometry(), row.rotationDeg(),
                row.label(), row.createdAt(), length, area);
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }

}
