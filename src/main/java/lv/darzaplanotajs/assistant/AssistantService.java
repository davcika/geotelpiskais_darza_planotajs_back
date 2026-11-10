package lv.darzaplanotajs.assistant;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lv.darzaplanotajs.garden.GardenObjectResponse;
import lv.darzaplanotajs.garden.GardenService;
import lv.darzaplanotajs.item.ItemEffectResponse;
import lv.darzaplanotajs.item.ItemService;
import lv.darzaplanotajs.item.ItemType;
import lv.darzaplanotajs.plant.PlantRepository;
import lv.darzaplanotajs.plant.PlantResponse;
import lv.darzaplanotajs.planting.PlantingResponse;
import lv.darzaplanotajs.planting.PlantingService;
import lv.darzaplanotajs.soil.SoilService;
import org.springframework.stereotype.Service;

/** Saliek dārza kontekstu un uzdod jautājumu modelim */
@Service
public class AssistantService {

    private static final ZoneId RIGA = ZoneId.of("Europe/Riga");

    private final GardenService gardens;
    private final PlantingService plantings;
    private final PlantRepository plants;
    private final GroqClient groq;
    private final SoilService soils;
    private final ItemService items;

    public AssistantService(GardenService gardens, PlantingService plantings, PlantRepository plants, GroqClient groq,
                            SoilService soils, ItemService items) {
        this.gardens = gardens;
        this.plantings = plantings;
        this.plants = plants;
        this.groq = groq;
        this.soils = soils;
        this.items = items;
    }

    public String ask(long userId, long gardenId, AssistantRequest request) {
        // Piederību pārbauda pirms kaut kas tiek sūtīts ārā
        List<GardenObjectResponse> objects = gardens.findObjects(userId, gardenId);
        List<PlantingResponse> season = plantings.findForGarden(userId, gardenId, request.year());
        Map<Long, PlantResponse> catalog = plants.findVisibleTo(userId).stream()
                .map(PlantResponse::from)
                .collect(Collectors.toMap(PlantResponse::id, Function.identity()));
        return groq.chat(systemPrompt(objects, season, catalog, request.year()).replace(DEFAULT_FROST, frostText(userId, gardenId)) + soilContext(gardenId, objects)
                        + itemContext(userId, gardenId, request.year(), objects, season, catalog),
                request.messages());
    }

    private static final String DEFAULT_FROST = "last spring frost around 25 May in Riga, first autumn frost late September";

    private String frostText(long userId, long gardenId) {
        var garden = gardens.findOwnGarden(userId, gardenId);
        if (garden.getLastFrost() == null || garden.getFirstFrost() == null) {
            return DEFAULT_FROST;
        }
        return "this garden's typical last spring frost " + garden.getLastFrost() + " and first autumn frost "
                + garden.getFirstFrost() + " (MM-DD)";
    }

    /** Dobes pašas augsne vai dārza noklusējums */
    private String soilContext(long gardenId, List<GardenObjectResponse> objects) {
        Map<Long, String> soil = soils.describeForGarden(gardenId);
        StringBuilder sb = new StringBuilder("\nSoil (bed override, else garden default; use it to say what fits where):\n");
        sb.append("- garden default: ").append(soil.getOrDefault(null, "not entered")).append("\n");
        for (GardenObjectResponse o : objects) {
            if (soil.containsKey(o.id())) {
                sb.append("- ").append(o.type()).append(" \"").append(o.name()).append("\": ").append(soil.get(o.id())).append("\n");
            }
        }
        return sb.toString();
    }

    /** Kuri dārza elementi sedz kuras dobes */
    private String itemContext(long userId, long gardenId, int year, List<GardenObjectResponse> objects,
                               List<PlantingResponse> season, Map<Long, PlantResponse> catalog) {
        if (items == null) {
            return "";
        }
        var placed = items.list(userId, gardenId);
        if (placed.isEmpty()) {
            return "";
        }
        Map<String, String> typeNames = items.types().stream()
                .collect(Collectors.toMap(ItemType::key, ItemType::nameEn));
        StringBuilder sb = new StringBuilder("\nGarden items (structures, protection, watering):\n");
        Map<String, Long> counts = placed.stream().collect(Collectors.groupingBy(
                i -> typeNames.getOrDefault(i.typeKey(), i.typeKey()), TreeMap::new, Collectors.counting()));
        counts.forEach((name, n) -> sb.append("- ").append(name).append(" x").append(n).append("\n"));
        Map<Long, String> bedNames = objects.stream()
                .collect(Collectors.toMap(GardenObjectResponse::id, GardenObjectResponse::name, (a, b) -> a));
        Map<Long, PlantingResponse> byId = season.stream()
                .collect(Collectors.toMap(PlantingResponse::id, Function.identity(), (a, b) -> a));
        Map<String, List<String>> perBed = new TreeMap<>();
        items.effects(userId, gardenId, year).forEach((plantingId, effect) -> {
            PlantingResponse p = byId.get(plantingId);
            if (p == null) {
                return;
            }
            PlantResponse plant = catalog.get(p.plantId());
            String by = effect.affectedBy().stream().map(ItemEffectResponse.AffectingItem::name)
                    .distinct().collect(Collectors.joining(", "));
            perBed.computeIfAbsent(bedNames.getOrDefault(p.objectId(), "Bed"), k -> new ArrayList<>())
                    .add((plant != null ? plant.nameEn() : "plant") + " under " + by + " (outdoor window "
                            + effect.outdoorFrom() + " to " + effect.outdoorTo() + ", harvest +"
                            + effect.harvestExtraDays() + " d)");
        });
        if (!perBed.isEmpty()) {
            sb.append("Active season protection per bed (dates already shifted, use these instead of catalog dates):\n");
            perBed.forEach((bed, list) -> sb.append("- \"").append(bed).append("\": ").append(String.join("; ", list)).append("\n"));
        }
        return sb.toString();
    }

    private static String systemPrompt(List<GardenObjectResponse> objects, List<PlantingResponse> season,
                                       Map<Long, PlantResponse> catalog, int year) {
        StringBuilder sb = new StringBuilder("""
                You are a friendly, practical vegetable garden assistant inside a garden planner app for Latvia
                (climate zone ~5b-6a, last spring frost around 25 May in Riga, first autumn frost late September).
                Answer in the language the user writes in (Latvian or English). Be concise: a few sentences or a short list. Format with simple Markdown only: short "### " headings, "- " or "1. " lists, **bold** for key words; no tables, links or HTML.
                Base advice on the user's garden data below. If the data does not cover the question, say so
                and give general advice for Latvia. Do not invent plantings that are not listed.
                Dates in the catalog are MM-DD and repeat every year.

                """);
        sb.append("Today: ").append(LocalDate.now(RIGA)).append(". Selected season: ").append(year).append("\n\n");

        sb.append("Garden objects:\n");
        if (objects.isEmpty()) {
            sb.append("- (none yet)\n");
        }
        for (GardenObjectResponse o : objects) {
            sb.append("- ").append(o.type()).append(" \"").append(o.name()).append("\"");
            if (o.gridCols() != null && o.gridRows() != null) {
                sb.append(", grid ").append(o.gridCols()).append("x").append(o.gridRows());
            }
            sb.append(": ");
            String planted = season.stream()
                    .filter(p -> p.objectId().equals(o.id()))
                    .map(p -> describe(p, catalog))
                    .collect(Collectors.joining("; "));
            sb.append(planted.isEmpty() ? "nothing planted this season" : planted).append("\n");
        }

        sb.append("\nPlant catalog (name EN / LV, category, family, method, indoor sow, outdoor sow/plant, then optional:"
                + " days to harvest, bloom, mature size height x spread, soil pH, light):\n");
        for (PlantResponse p : catalog.values()) {
            sb.append("- ").append(p.nameEn()).append(" / ").append(p.nameLv())
                    .append(", ").append(p.category()).append(", ").append(p.family()).append(", ").append(p.sowMethod())
                    .append(", indoor ").append(range(p.indoorSowFrom(), p.indoorSowTo()))
                    .append(", outdoor ").append(range(p.outdoorFrom(), p.outdoorTo()));
            if (p.daysToHarvest() != null) sb.append(", harvest after ").append(p.daysToHarvest()).append(" d");
            if (p.bloomFrom() != null) sb.append(", blooms ").append(range(p.bloomFrom(), p.bloomTo()));
            if (p.evergreen()) sb.append(", evergreen");
            if (p.heightM() != null) sb.append(", mature ").append(p.heightM()).append("x").append(p.spreadM()).append(" m");
            if (p.phMin() != null) sb.append(", pH ").append(p.phMin()).append("-").append(p.phMax());
            if (p.light() != null) sb.append(", ").append(p.light());
            sb.append(p.frostSensitive() ? ", frost sensitive" : "").append("\n");
        }
        return sb.toString();
    }

    private static String describe(PlantingResponse p, Map<Long, PlantResponse> catalog) {
        PlantResponse plant = catalog.get(p.plantId());
        String name = plant == null ? "unknown plant" : plant.nameEn() + " (" + plant.family() + ")";
        if (p.variety() != null && !p.variety().isBlank()) {
            name += " '" + p.variety() + "'";
        }
        return name + " sown " + p.sownOn() + (p.cells() == null ? "" : ", " + p.cells().size() + " cells");
    }

    private static String range(String from, String to) {
        return from == null ? "-" : from + ".." + to;
    }
}
