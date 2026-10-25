package lv.darzaplanotajs.soil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lv.darzaplanotajs.assistant.ChatMessage;
import lv.darzaplanotajs.assistant.GroqClient;
import lv.darzaplanotajs.common.ApiErrors;
import lv.darzaplanotajs.garden.GardenObject;
import lv.darzaplanotajs.garden.GardenService;
import lv.darzaplanotajs.garden.ObjectType;
import lv.darzaplanotajs.plant.PlantRepository;
import lv.darzaplanotajs.plant.PlantResponse;
import lv.darzaplanotajs.planting.PlantingResponse;
import lv.darzaplanotajs.planting.PlantingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Piederību pārbauda caur GardenService, svešam 404 */
@Service
@Transactional
public class SoilService {

    private static final int SUGGESTIONS = 8;

    private final SoilProfileRepository soils;
    private final GardenService gardens;
    private final PlantingService plantings;
    private final PlantRepository plants;
    private final GroqClient groq;

    public SoilService(SoilProfileRepository soils, GardenService gardens, PlantingService plantings,
                       PlantRepository plants, GroqClient groq) {
        this.soils = soils;
        this.gardens = gardens;
        this.plantings = plantings;
        this.plants = plants;
        this.groq = groq;
    }

    @Transactional(readOnly = true)
    public SoilResponse getGarden(long userId, long gardenId) {
        gardens.findOwnGarden(userId, gardenId);
        return SoilResponse.from(soils.findByGardenIdAndObjectIdIsNull(gardenId).orElse(null));
    }

    public SoilResponse putGarden(long userId, long gardenId, SoilRequest request) {
        gardens.findOwnGarden(userId, gardenId);
        SoilProfile soil = soils.findByGardenIdAndObjectIdIsNull(gardenId)
                .orElseGet(() -> new SoilProfile(gardenId, null));
        soil.update(request);
        return SoilResponse.from(soils.save(soil));
    }

    @Transactional(readOnly = true)
    public BedSoilResponse getBed(long userId, long objectId) {
        GardenObject bed = gardens.findOwnObject(userId, objectId);
        return bedSoil(bed);
    }

    public BedSoilResponse putBed(long userId, long objectId, SoilRequest request) {
        GardenObject bed = gardens.findOwnObject(userId, objectId);
        if (bed.getType() != ObjectType.BED) {
            throw ApiErrors.badRequest("Soil can only be set for beds");
        }
        SoilProfile soil = soils.findByObjectId(objectId).orElseGet(() -> new SoilProfile(bed.getGardenId(), objectId));
        soil.update(request);
        soils.save(soil);
        return bedSoil(bed);
    }

    /** Pēc tam dobe atkal lieto dārza noklusējumu */
    public void deleteBed(long userId, long objectId) {
        gardens.findOwnObject(userId, objectId);
        soils.findByObjectId(objectId).ifPresent(soils::delete);
    }

    @Transactional(readOnly = true)
    public SuitabilityResponse suitability(long userId, long objectId, int year) {
        GardenObject bed = gardens.findOwnObject(userId, objectId);
        SoilResponse soil = bedSoil(bed).effective();
        Map<Long, PlantResponse> catalog = catalog();
        List<PlantingResponse> inBed = bedPlantings(userId, bed, year);

        List<SuitabilityResponse.Item> planted = inBed.stream()
                .filter(p -> catalog.containsKey(p.plantId()))
                .map(p -> item(p.id(), catalog.get(p.plantId()), soil))
                .toList();

        Set<Long> plantedIds = inBed.stream().map(PlantingResponse::plantId).collect(Collectors.toSet());
        List<SuitabilityResponse.Item> suggestions = soil == null ? List.of() : catalog.values().stream()
                .filter(p -> !plantedIds.contains(p.id()))
                .map(p -> Map.entry(p, SoilSuitability.assess(p, soil)))
                .filter(e -> e.getValue().level() == SoilSuitability.Level.GOOD && e.getValue().checks() > 0)
                .sorted(Comparator.comparingInt((Map.Entry<PlantResponse, SoilSuitability.Verdict> e) -> -e.getValue().checks())
                        .thenComparing(e -> e.getKey().nameEn()))
                .limit(SUGGESTIONS)
                .map(e -> new SuitabilityResponse.Item(null, e.getKey().id(), e.getKey().nameEn(), e.getKey().nameLv(),
                        e.getValue().level().name(), e.getValue().reasons()))
                .toList();
        return new SuitabilityResponse(soil, planted, suggestions);
    }

    /** Groq iesaka, kas kur derēs, pēc augsnes un stādījumiem */
    public AdviceResponse advice(long userId, long objectId, int year) {
        GardenObject bed = gardens.findOwnObject(userId, objectId);
        SuitabilityResponse fit = suitability(userId, objectId, year);
        StringBuilder sb = new StringBuilder("""
                You are a practical garden soil advisor for Latvia. Answer in English, concise: at most 6 short bullet
                points. Say which plants fit this bed's soil and light, which planted ones may struggle and what to do
                (liming, compost, drainage, moving to another spot). Use only the data below; do not invent soil values.

                """);
        sb.append("Bed \"").append(bed.getName()).append("\", season ").append(year).append("\n");
        sb.append("Soil: ").append(fit.soil() == null ? "unknown (no data entered)" : fit.soil().describe()).append("\n");
        if (fit.soil() != null && fit.soil().labNote() != null) sb.append("Lab note: ").append(fit.soil().labNote()).append("\n");
        sb.append("Planted (rule-based verdict):\n");
        if (fit.plantings().isEmpty()) sb.append("- nothing planted\n");
        fit.plantings().forEach(i -> sb.append("- ").append(i.nameEn()).append(": ").append(i.level())
                .append(" (").append(String.join("; ", i.reasons())).append(")\n"));
        sb.append("Catalog plants that fit by rules:\n");
        if (fit.suggestions().isEmpty()) sb.append("- (none determined)\n");
        fit.suggestions().forEach(i -> sb.append("- ").append(i.nameEn()).append("\n"));
        String answer = groq.chat(sb.toString(),
                List.of(new ChatMessage("user", "What fits best in this bed and what should I change?")));
        return new AdviceResponse(answer);
    }

    /** Asistentam, atslēga null ir dārza noklusējums */
    @Transactional(readOnly = true)
    public Map<Long, String> describeForGarden(long gardenId) {
        Map<Long, String> out = new HashMap<>();
        soils.findByGardenId(gardenId).forEach(s -> out.put(s.objectId, SoilResponse.from(s).describe()));
        return out;
    }

    private BedSoilResponse bedSoil(GardenObject bed) {
        SoilResponse own = SoilResponse.from(soils.findByObjectId(bed.getId()).orElse(null));
        SoilResponse def = SoilResponse.from(soils.findByGardenIdAndObjectIdIsNull(bed.getGardenId()).orElse(null));
        return new BedSoilResponse(bed.getGardenId(), own, def, own != null ? own : def);
    }

    private List<PlantingResponse> bedPlantings(long userId, GardenObject bed, int year) {
        return plantings.findForGarden(userId, bed.getGardenId(), year).stream()
                .filter(p -> bed.getId().equals(p.objectId()))
                .toList();
    }

    private Map<Long, PlantResponse> catalog() {
        return plants.findAllByOrderByNameEn().stream().map(PlantResponse::from)
                .collect(Collectors.toMap(PlantResponse::id, p -> p, (a, b) -> a, java.util.LinkedHashMap::new));
    }

    private static SuitabilityResponse.Item item(Long plantingId, PlantResponse p, SoilResponse soil) {
        SoilSuitability.Verdict v = SoilSuitability.assess(p, soil);
        return new SuitabilityResponse.Item(plantingId, p.id(), p.nameEn(), p.nameLv(), v.level().name(), v.reasons());
    }
}
