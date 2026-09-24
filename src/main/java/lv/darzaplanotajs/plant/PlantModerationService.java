package lv.darzaplanotajs.plant;

import java.util.List;
import java.util.Locale;
import lv.darzaplanotajs.assistant.ChatMessage;
import lv.darzaplanotajs.assistant.GroqClient;
import lv.darzaplanotajs.auth.AppUserRepository;
import lv.darzaplanotajs.common.ApiErrors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** AI tikai iesaka, lēmumu pieņem admins, un bez AI viss pārējais strādā */
@Service
public class PlantModerationService {

    /** Trigrammu līdzības slieksnis dublikātu kandidātiem */
    private static final double SIMILARITY = 0.3;
    private static final String NORM_EN = "lower(unaccent(name_en))";
    private static final String NORM_LV = "lower(unaccent(name_lv))";

    private final PlantRepository plants;
    private final AppUserRepository users;
    private final JdbcTemplate jdbc;
    private final GroqClient groq;

    public PlantModerationService(PlantRepository plants, AppUserRepository users, JdbcTemplate jdbc, GroqClient groq) {
        this.plants = plants;
        this.users = users;
        this.jdbc = jdbc;
        this.groq = groq;
    }

    /** Lomu ņem no DB, nevis JWT, lai izmaiņas strādātu uzreiz bez jauna tokena */
    public boolean isAdmin(long userId) {
        return users.findById(userId).map(u -> u.isAdmin()).orElse(false);
    }

    public void requireAdmin(long userId) {
        if (!isAdmin(userId)) {
            throw ApiErrors.forbidden("Admins only");
        }
    }

    public List<PendingPlantResponse> pending() {
        return plants.findByStatusOrderBySubmittedAt(PlantStatus.PENDING.name()).stream()
                .map(p -> new PendingPlantResponse(PlantResponse.from(p),
                        users.findById(p.ownerId).map(u -> u.getEmail()).orElse(null),
                        p.submittedAt, similarApproved(p)))
                .toList();
    }

    /** Angļu un latviešu nosaukumus salīdzina krusteniski */
    List<SimilarPlant> similarApproved(Plant p) {
        String en = "lower(unaccent(?))";
        String score = "GREATEST(similarity(" + NORM_EN + ", " + en + "), similarity(" + NORM_LV + ", " + en
                + "), similarity(" + NORM_EN + ", " + en + "), similarity(" + NORM_LV + ", " + en + "))";
        return jdbc.query("SELECT * FROM (SELECT id, name_en, name_lv, " + score + " AS score FROM plant"
                        + " WHERE status = 'APPROVED' AND id <> ?) s WHERE score >= ? ORDER BY score DESC LIMIT 5",
                (rs, i) -> new SimilarPlant(rs.getLong("id"), rs.getString("name_en"), rs.getString("name_lv"),
                        Math.round(rs.getDouble("score") * 100) / 100.0),
                p.nameEn, p.nameEn, p.nameLv, p.nameLv, p.id, SIMILARITY);
    }

    /** Ja Groq nav pieejams, available ir false */
    public AiReviewResponse aiReview(long id) {
        Plant p = pendingPlant(id);
        List<SimilarPlant> similar = similarApproved(p);
        String system = """
                You moderate a community garden plant catalog for Latvia (climate zone ~5b). A user submitted a plant.
                Judge: is it a real plant with plausible data (names EN/LV, botanical family, sowing/planting windows
                MM-DD for Latvia, harvest days, size, pH, light)? Is it spam, a joke or junk? Is it a duplicate of a
                listed existing plant? Answer in English, at most 6 short lines.
                First line exactly: VERDICT: APPROVE, VERDICT: FIX or VERDICT: REJECT.
                Then bullet lines starting with "- " with reasons and concrete suggested fixes.""";
        String user = "Submitted plant: " + describe(p) + "\nSimilar existing plants: "
                + (similar.isEmpty() ? "none" : similar.stream()
                .map(s -> s.nameEn() + " / " + s.nameLv() + " (similarity " + s.score() + ")").toList());
        try {
            String answer = groq.chat(system, List.of(new ChatMessage("user", user)));
            return AiReviewResponse.parse(answer);
        } catch (ResponseStatusException e) {
            return AiReviewResponse.unavailable(e.getReason());
        }
    }

    @Transactional
    public PlantResponse approve(long id) {
        Plant p = pendingPlant(id);
        if (plants.existsApprovedName(p.nameEn)) {
            throw ApiErrors.conflict(
                    "An approved plant named '" + p.nameEn + "' already exists; reject as duplicate");
        }
        p.status = PlantStatus.APPROVED.name();
        p.reviewNote = null;
        return PlantResponse.from(plants.saveAndFlush(p));
    }

    @Transactional
    public PlantResponse reject(long id, String note) {
        Plant p = pendingPlant(id);
        p.status = PlantStatus.REJECTED.name();
        p.reviewNote = note == null || note.isBlank() ? null : note.trim();
        return PlantResponse.from(plants.save(p));
    }

    private Plant pendingPlant(long id) {
        return plants.findById(id)
                .filter(p -> p.statusEnum() == PlantStatus.PENDING)
                .orElseThrow(() -> ApiErrors.notFound("Pending plant", id));
    }

    private static String describe(Plant p) {
        return String.format(Locale.ROOT,
                "nameEn=%s; nameLv=%s; family=%s; category=%s; sowMethod=%s; indoorSow=%s..%s; outdoor=%s..%s;"
                        + " daysToHarvest=%s; harvestWindowDays=%s; frostSensitive=%s; perennial=%s; evergreen=%s;"
                        + " foliage=%s..%s; bloom=%s..%s; bloomColor=%s; heightM=%s; spreadM=%s; yearsToMature=%s;"
                        + " pH=%s..%s; light=%s",
                p.nameEn, p.nameLv, p.family, p.category, p.sowMethod, p.indoorSowFrom, p.indoorSowTo,
                p.outdoorFrom, p.outdoorTo, p.daysToHarvest, p.harvestWindowDays, p.frostSensitive, p.perennial,
                p.evergreen, p.foliageFrom, p.foliageTo, p.bloomFrom, p.bloomTo, p.bloomColor, p.heightM, p.spreadM,
                p.yearsToMature, p.phMin, p.phMax, p.light);
    }
}
