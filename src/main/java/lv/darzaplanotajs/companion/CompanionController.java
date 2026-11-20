package lv.darzaplanotajs.companion;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Visi augu kaimiņu pāri, dobi pārbauda frontend */
@RestController
public class CompanionController {

    /** Katrs pāris glabājas vienreiz, plantAId < plantBId */
    public record CompanionPair(long plantAId, long plantBId, String relation) {}

    private final JdbcTemplate jdbc;

    public CompanionController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/plants/companions")
    public List<CompanionPair> list() {
        return jdbc.query("SELECT plant_a_id, plant_b_id, relation FROM plant_companion",
                (rs, i) -> new CompanionPair(rs.getLong(1), rs.getLong(2), rs.getString(3)));
    }
}
