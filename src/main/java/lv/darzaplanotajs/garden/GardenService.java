package lv.darzaplanotajs.garden;

import java.util.List;
import java.util.Objects;
import lv.darzaplanotajs.common.ApiErrors;
import lv.darzaplanotajs.address.AddressResponse;
import lv.darzaplanotajs.address.AddressSearch;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Svešs dārzs izskatās tāpat kā neesošs, lai neatklātu, ka tāds ir.
 * Ja dārzam ir zona, visiem tā objektiem jābūt zonā.
 */
@Service
@Transactional
public class GardenService {

    private final GardenRepository gardens;
    private final GardenObjectRepository objects;
    private final AddressSearch addresses;
    private final SharingRepository sharing;

    public GardenService(GardenRepository gardens, GardenObjectRepository objects, AddressSearch addresses,
                         SharingRepository sharing) {
        this.gardens = gardens;
        this.objects = objects;
        this.addresses = addresses;
        this.sharing = sharing;
    }

    /** Reģistrējoties dārzs vairs netiek veidots automātiski */
    public GardenResponse createGarden(long userId, GardenRequest request) {
        return response(userId, gardens.save(new Garden(userId, request.name().strip(), request.icon(), request.color())));
    }

    public GardenResponse updateGarden(long userId, long gardenId, GardenRequest request) {
        Garden garden = findOwnGarden(userId, gardenId);
        garden.rename(request.name().strip(), request.icon(), request.color());
        return response(userId, garden);
    }

    /** Visu pārējo izdzēš pati DB ar ON DELETE CASCADE */
    public void deleteGarden(long userId, long gardenId) {
        gardens.delete(findOwnedGarden(userId, gardenId));
    }

    public Garden applyFrost(long userId, long gardenId, String source, String last, String first,
                             FrostCalculator.Estimate range, Double lat, Double lon) {
        Garden garden = findOwnGarden(userId, gardenId);
        garden.setFrost(source, last, first, range, lat, lon);
        return garden;
    }

    @Transactional(readOnly = true)
    public List<GardenResponse> findGardens(long userId) {
        return gardens.findAccessible(userId).stream()
                .map(garden -> response(userId, garden))
                .toList();
    }

    /** Noraida, ja kāds esošs objekts paliktu ārpus zonas */
    public GardenResponse setHub(long userId, long gardenId, HubRequest request) {
        Garden garden = findOwnGarden(userId, gardenId);
        Polygon zone = ZoneRules.validZone(request.geometry());
        String addressLabel = request.addressCode() == null ? null : addresses.findByCode(request.addressCode())
                .map(AddressResponse::label)
                .orElseThrow(() -> ApiErrors.badRequest("Unknown address code " + request.addressCode()));

        long outside = objects.findByGardenIdOrderById(gardenId).stream()
                .filter(object -> !ZoneRules.contains(zone, object.getGeometry()))
                .count();
        if (outside > 0) {
            throw ApiErrors.badRequest(outside + " objects are outside the zone");
        }
        garden.setHub(request.addressCode(), addressLabel, request.geometry());
        return response(userId, garden);
    }

    @Transactional(readOnly = true)
    public List<GardenObjectResponse> findObjects(long userId, long gardenId) {
        findOwnGarden(userId, gardenId);
        return objects.findByGardenIdOrderById(gardenId).stream()
                .map(GardenObjectResponse::from)
                .toList();
    }

    public GardenObjectResponse createObject(long userId, long gardenId, GardenObjectRequest request) {
        requireInsideZone(findOwnGarden(userId, gardenId), request.geometry());
        requireWoodyPlant(request);
        GardenObject object = new GardenObject(gardenId);
        object.update(request);
        return GardenObjectResponse.from(objects.save(object));
    }

    public GardenObjectResponse updateObject(long userId, long id, GardenObjectRequest request) {
        GardenObject object = findOwnObject(userId, id);
        requireInsideZone(findOwnGarden(userId, object.getGardenId()), request.geometry());
        requireGridUnchangedIfPainted(object, request);
        requireWoodyPlant(request);
        object.update(request);
        return GardenObjectResponse.from(object);
    }

    public void deleteObject(long userId, long id) {
        objects.delete(findOwnObject(userId, id));
    }

    /** Iekrāsotās šūnas ir piesaistītas režģim, tāpēc dobi drīkst pārvietot, bet ne mainīt tās izmērus */
    private void requireGridUnchangedIfPainted(GardenObject object, GardenObjectRequest request) {
        if (object.getType() != ObjectType.BED || !objects.hasPaintedCells(object.getId())) {
            return;
        }
        Envelope before = ZoneRules.parse(object.getGeometry()).getEnvelopeInternal();
        Envelope after = ZoneRules.parse(request.geometry()).getEnvelopeInternal();
        boolean sameSize = Math.abs(before.getWidth() - after.getWidth()) < 1e-6
                && Math.abs(before.getHeight() - after.getHeight()) < 1e-6;
        if (!sameSize || !Objects.equals(object.getGridCols(), request.gridCols())
                || !Objects.equals(object.getGridRows(), request.gridRows())) {
            throw ApiErrors.badRequest("Clear the painted cells before changing the bed's shape or grid");
        }
    }

    /** Kokam piesaistītajam augam jābūt katalogā kā kokam, krūmam vai dzīvžogam */
    private void requireWoodyPlant(GardenObjectRequest request) {
        if (request.plantId() != null && !objects.isWoodyPlant(request.plantId())) {
            throw ApiErrors.badRequest("Pick a tree, shrub or hedge from the catalog");
        }
    }

    /** Dārzi bez zonas (pirms onboarding) objektus neierobežo */
    private static void requireInsideZone(Garden garden, String objectGeometry) {
        if (garden.getHubGeometry() == null) {
            return;
        }
        Geometry zone = ZoneRules.parse(garden.getHubGeometry());
        if (!ZoneRules.contains(zone, objectGeometry)) {
            throw ApiErrors.badRequest("Object must be inside the garden zone");
        }
    }

    /** Visas dārza piekļuves pārbaudes iet caur šo un findOwnObject */
    public Garden findOwnGarden(long userId, long gardenId) {
        return gardens.findAccessible(gardenId, userId).orElseThrow(() -> ApiErrors.notFound("Garden", gardenId));
    }

    public boolean canAccess(long userId, long gardenId) {
        return gardens.findAccessible(gardenId, userId).isPresent();
    }

    /** Tikai īpašniekam, redaktoram 403 un svešam 404 */
    public Garden findOwnedGarden(long userId, long gardenId) {
        Garden garden = findOwnGarden(userId, gardenId);
        if (!garden.getOwnerId().equals(userId)) {
            throw ApiErrors.forbidden("Only the garden owner can do this");
        }
        return garden;
    }

    /** Objekts dārzā, ko lietotājs drīkst mainīt, citādi 404 */
    public GardenObject findOwnObject(long userId, long id) {
        return objects.findById(id)
                .filter(object -> canAccess(userId, object.getGardenId()))
                .orElseThrow(() -> ApiErrors.notFound("Object", id));
    }

    GardenResponse response(long userId, Garden garden) {
        String role = garden.getOwnerId().equals(userId) ? "OWNER" : "EDITOR";
        return GardenResponse.from(garden, role, sharing.email(garden.getOwnerId()).orElse(null));
    }

}
