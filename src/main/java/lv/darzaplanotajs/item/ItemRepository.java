package lv.darzaplanotajs.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/** Stādījumus un dobes tikai lasa, citu pakotņu klases nemaina */
@Repository
class ItemRepository {

    private final JdbcTemplate jdbc;

    ItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record ItemRow(long id, long gardenId, String typeKey, String geometry, double rotationDeg, String label,
                   Instant createdAt) {
    }

    /** Bloks var būt null */
    record PlantingRow(long id, String bedGeometry, Double blockX, Double blockY, Double blockW, Double blockH,
                       String outdoorFrom, String outdoorTo) {
    }

    List<ItemType> types() {
        return jdbc.query("SELECT * FROM item_type ORDER BY sort_order", ItemRepository::type);
    }

    Optional<ItemType> type(String key) {
        return jdbc.query("SELECT * FROM item_type WHERE key = ?", ItemRepository::type, key).stream().findFirst();
    }

    List<ItemRow> findByGarden(long gardenId) {
        return jdbc.query("SELECT * FROM garden_item WHERE garden_id = ? ORDER BY id", ItemRepository::item, gardenId);
    }

    Optional<ItemRow> findById(long id) {
        return jdbc.query("SELECT * FROM garden_item WHERE id = ?", ItemRepository::item, id).stream().findFirst();
    }

    long insert(long gardenId, GardenItemRequest r) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement("INSERT INTO garden_item (garden_id, item_type_key, geometry, rotation_deg, label)"
                    + " VALUES (?, ?, ?, ?, ?)", new String[] {"id"});
            ps.setLong(1, gardenId);
            ps.setString(2, r.typeKey());
            ps.setString(3, r.geometry());
            ps.setDouble(4, r.rotationDeg() == null ? 0 : r.rotationDeg());
            ps.setString(5, blankToNull(r.label()));
            return ps;
        }, keys);
        return keys.getKey().longValue();
    }

    void update(long id, GardenItemRequest r) {
        jdbc.update("UPDATE garden_item SET item_type_key = ?, geometry = ?, rotation_deg = ?, label = ? WHERE id = ?",
                r.typeKey(), r.geometry(), r.rotationDeg() == null ? 0 : r.rotationDeg(), blankToNull(r.label()), id);
    }

    void delete(long id) {
        jdbc.update("DELETE FROM garden_item WHERE id = ?", id);
    }

    List<PlantingRow> plantings(long gardenId, int year) {
        return jdbc.query("""
                SELECT pl.id, o.geometry, pl.block_x, pl.block_y, pl.block_w, pl.block_h, p.outdoor_from, p.outdoor_to
                FROM planting pl
                JOIN garden_object o ON o.id = pl.garden_object_id
                JOIN plant p ON p.id = pl.plant_id
                WHERE o.garden_id = ? AND EXTRACT(YEAR FROM pl.sown_on) = ?
                ORDER BY pl.id""", (rs, n) -> new PlantingRow(rs.getLong(1), rs.getString(2),
                rs.getObject(3, Double.class), rs.getObject(4, Double.class), rs.getObject(5, Double.class),
                rs.getObject(6, Double.class), rs.getString(7), rs.getString(8)), gardenId, year);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static ItemType type(ResultSet rs, int n) throws SQLException {
        return new ItemType(rs.getString("key"), rs.getString("name_en"), rs.getString("name_lv"),
                rs.getString("icon"), rs.getString("shape"), rs.getString("category"),
                rs.getObject("default_w_m", Double.class), rs.getObject("default_h_m", Double.class),
                rs.getInt("start_shift_days"), rs.getInt("end_shift_days"), rs.getInt("harvest_shift_days"),
                rs.getDouble("frost_protection_c"), rs.getBoolean("climbing_support"));
    }

    private static ItemRow item(ResultSet rs, int n) throws SQLException {
        return new ItemRow(rs.getLong("id"), rs.getLong("garden_id"), rs.getString("item_type_key"),
                rs.getString("geometry"), rs.getDouble("rotation_deg"), rs.getString("label"),
                rs.getTimestamp("created_at").toInstant());
    }
}
