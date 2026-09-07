package lv.darzaplanotajs.address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * Vispirms meklē katru vārdu kā apakšvirkni, un ja nekas neatrodas, tad ar trigrammu līdzību.
 * Tā brivbas atrod arī Brīvības.
 */
@Service
public class PostgresAddressSearch implements AddressSearch {

    private static final String WORD = normalized("?");
    private static final String CONTAINS_WORD = "search_text LIKE '%' || " + WORD + " || '%'";
    private static final String SIMILAR_WORD = WORD + " <<% search_text";
    private static final String SELECT = "SELECT code, label, east, north FROM address ";
    private static final RowMapper<AddressResponse> ROW = (rs, i) -> new AddressResponse(
            rs.getLong("code"), rs.getString("label"), rs.getDouble("east"), rs.getDouble("north"));

    private final JdbcTemplate jdbc;

    public PostgresAddressSearch(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Normalizācija ir tikai šeit, to lieto arī importētājs */
    static String normalized(String sqlExpression) {
        return "lower(unaccent(" + sqlExpression + "))";
    }

    @Override
    public boolean isReady() {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM address)", Boolean.class));
    }

    @Override
    public List<AddressResponse> search(String rawQuery, int limit) {
        // LIKE speciālsimbolus izmet, citādi %%% atbilstu visām rindām
        String query = rawQuery.replaceAll("[%_\\\\]", " ").trim();
        if (query.isEmpty()) {
            return List.of();
        }
        List<AddressResponse> exact = findWhereEveryWord(CONTAINS_WORD, query, limit);
        return exact.isEmpty() ? findWhereEveryWord(SIMILAR_WORD, query, limit) : exact;
    }

    @Override
    public Optional<AddressResponse> findByCode(long code) {
        return jdbc.query(SELECT + "WHERE code = ?", ROW, code).stream().findFirst();
    }

    /** Katram vārdam jāatbilst, labākās sakritības pirmās */
    private List<AddressResponse> findWhereEveryWord(String wordCondition, String query, int limit) {
        List<String> words = Arrays.asList(query.split("\\s+"));
        String where = String.join(" AND ", words.stream().map(word -> wordCondition).toList());

        List<Object> params = new ArrayList<>(words);
        params.add(query);
        params.add(limit);
        return jdbc.query(SELECT + "WHERE " + where
                        + " ORDER BY similarity(search_text, " + WORD + ") DESC, length(label), label LIMIT ?",
                ROW, params.toArray());
    }
}
