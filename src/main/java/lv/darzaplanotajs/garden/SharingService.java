package lv.darzaplanotajs.garden;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import lv.darzaplanotajs.common.ApiErrors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Kas atver saiti un pievienojas, kļūst par redaktoru un drīkst mainīt visu dārzā.
 * Dzēst dārzu un pārvaldīt dalībniekus vai saiti var tikai īpašnieks.
 */
@Service
@Transactional
public class SharingService {

    static final Duration INVITE_VALID = Duration.ofDays(7);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GardenService gardens;
    private final GardenRepository gardenRepository;
    private final SharingRepository sharing;

    public SharingService(GardenService gardens, GardenRepository gardenRepository, SharingRepository sharing) {
        this.gardens = gardens;
        this.gardenRepository = gardenRepository;
        this.sharing = sharing;
    }

    public record InviteResponse(String token, Instant expiresAt) {
    }

    public record InvitePreview(long gardenId, String gardenName, String ownerEmail, boolean alreadyMember) {
    }

    public record MemberResponse(long userId, String email, String role, boolean you) {
    }

    /** Vecā saite pēc tam vairs nederēs */
    public InviteResponse createInvite(long userId, long gardenId) {
        gardens.findOwnedGarden(userId, gardenId);
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plus(INVITE_VALID);
        sharing.saveInvite(gardenId, token, expiresAt);
        return new InviteResponse(token, expiresAt);
    }

    /** Lai iestatījumos saiti varētu parādīt vēlreiz */
    @Transactional(readOnly = true)
    public Optional<InviteResponse> currentInvite(long userId, long gardenId) {
        gardens.findOwnedGarden(userId, gardenId);
        return sharing.findValidInviteOfGarden(gardenId).map(row -> new InviteResponse(row.token(), row.expiresAt()));
    }

    public void revokeInvite(long userId, long gardenId) {
        gardens.findOwnedGarden(userId, gardenId);
        sharing.deleteInvite(gardenId);
    }

    @Transactional(readOnly = true)
    public InvitePreview preview(long userId, String token) {
        Garden garden = invitedGarden(token);
        boolean member = garden.getOwnerId().equals(userId) || sharing.isMember(garden.getId(), userId);
        return new InvitePreview(garden.getId(), garden.getName(),
                sharing.email(garden.getOwnerId()).orElse(null), member);
    }

    /** Īpašniekam vai esošam dalībniekam neko nemaina */
    public GardenResponse accept(long userId, String token) {
        Garden garden = invitedGarden(token);
        if (!garden.getOwnerId().equals(userId)) {
            sharing.addMember(garden.getId(), userId);
        }
        return gardens.response(userId, garden);
    }

    /** Īpašnieks pirmais, tad redaktori */
    @Transactional(readOnly = true)
    public List<MemberResponse> members(long userId, long gardenId) {
        Garden garden = gardens.findOwnGarden(userId, gardenId);
        List<MemberResponse> result = new ArrayList<>();
        long ownerId = garden.getOwnerId();
        result.add(new MemberResponse(ownerId, sharing.email(ownerId).orElse(null), "OWNER", ownerId == userId));
        for (SharingRepository.MemberRow row : sharing.members(gardenId)) {
            result.add(new MemberResponse(row.userId(), row.email(), "EDITOR", row.userId() == userId));
        }
        return result;
    }

    /** Īpašnieks izņem redaktoru, vai redaktors pats iziet no dārza */
    public void removeMember(long userId, long gardenId, long memberId) {
        Garden garden = gardens.findOwnGarden(userId, gardenId);
        if (garden.getOwnerId().equals(memberId)) {
            throw ApiErrors.badRequest("The owner cannot leave their own garden");
        }
        if (memberId != userId && !garden.getOwnerId().equals(userId)) {
            throw ApiErrors.forbidden("Only the garden owner can remove people");
        }
        if (!sharing.removeMember(gardenId, memberId)) {
            throw ApiErrors.notFound("Member", memberId);
        }
    }

    /** Nederīga, beigusies vai atsaukta saite izskatās vienādi */
    private Garden invitedGarden(String token) {
        return sharing.findValidInvite(token)
                .flatMap(invite -> gardenRepository.findById(invite.gardenId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "This invite link is invalid or has expired"));
    }
}
