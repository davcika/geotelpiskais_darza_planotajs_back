package lv.darzaplanotajs.garden;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/** Dārza dalībnieki un uzaicinājuma saites */
@Repository
class SharingRepository {

    private final JdbcTemplate jdbc;

    SharingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record InviteRow(long gardenId, String token, Instant expiresAt) {
    }

    record MemberRow(long userId, String email) {
    }

    boolean isMember(long gardenId, long userId) {
        Boolean found = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM garden_member WHERE garden_id = ? AND user_id = ?)",
                Boolean.class, gardenId, userId);
        return Boolean.TRUE.equals(found);
    }

    /** Dārzi, kuros lietotājs ir redaktors */
    List<Long> gardenIdsOfMember(long userId) {
        return jdbc.queryForList("SELECT garden_id FROM garden_member WHERE user_id = ?", Long.class, userId);
    }

    /** Atkārtota pievienošanās nav kļūda */
    void addMember(long gardenId, long userId) {
        jdbc.update("INSERT INTO garden_member (garden_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                gardenId, userId);
    }

    boolean removeMember(long gardenId, long userId) {
        return jdbc.update("DELETE FROM garden_member WHERE garden_id = ? AND user_id = ?", gardenId, userId) > 0;
    }

    /** Redaktori (bez īpašnieka) pievienošanās secībā */
    List<MemberRow> members(long gardenId) {
        return jdbc.query("""
                SELECT u.id, u.email FROM garden_member m JOIN app_user u ON u.id = m.user_id
                WHERE m.garden_id = ? ORDER BY m.joined_at, u.id
                """, (rs, i) -> new MemberRow(rs.getLong("id"), rs.getString("email")), gardenId);
    }

    Optional<String> email(long userId) {
        return jdbc.queryForList("SELECT email FROM app_user WHERE id = ?", String.class, userId).stream().findFirst();
    }

    void saveInvite(long gardenId, String token, Instant expiresAt) {
        jdbc.update("""
                INSERT INTO garden_invite (garden_id, token, expires_at) VALUES (?, ?, ?)
                ON CONFLICT (garden_id) DO UPDATE SET token = EXCLUDED.token, created_at = now(),
                    expires_at = EXCLUDED.expires_at
                """, gardenId, token, Timestamp.from(expiresAt));
    }

    void deleteInvite(long gardenId) {
        jdbc.update("DELETE FROM garden_invite WHERE garden_id = ?", gardenId);
    }

    private static final RowMapper<InviteRow> INVITE = (rs, i) -> new InviteRow(rs.getLong("garden_id"),
            rs.getString("token"), rs.getTimestamp("expires_at").toInstant());

    Optional<InviteRow> findValidInviteOfGarden(long gardenId) {
        return jdbc.query("SELECT * FROM garden_invite WHERE garden_id = ? AND expires_at > now()",
                INVITE,
                gardenId).stream().findFirst();
    }

    /** Tikai vēl derīga saite */
    Optional<InviteRow> findValidInvite(String token) {
        return jdbc.query("SELECT * FROM garden_invite WHERE token = ? AND expires_at > now()",
                INVITE,
                token).stream().findFirst();
    }
}
