package lv.darzaplanotajs.plant;

import static lv.darzaplanotajs.common.CurrentUser.userId;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Augu moderācija tikai adminiem */
@RestController
@RequestMapping("/api/admin")
@Validated
public class PlantAdminController {

    private final PlantModerationService moderation;

    public PlantAdminController(PlantModerationService moderation) {
        this.moderation = moderation;
    }

    /** Pieejams visiem, frontend pēc tā rāda vai slēpj moderācijas saiti */
    @GetMapping("/me")
    public Map<String, Boolean> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("admin", moderation.isAdmin(userId(jwt)));
    }

    @GetMapping("/plants/pending")
    public List<PendingPlantResponse> pending(@AuthenticationPrincipal Jwt jwt) {
        moderation.requireAdmin(userId(jwt));
        return moderation.pending();
    }

    @GetMapping("/plants/{id}/ai-review")
    public AiReviewResponse aiReview(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        moderation.requireAdmin(userId(jwt));
        return moderation.aiReview(id);
    }

    @PostMapping("/plants/{id}/approve")
    public PlantResponse approve(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        moderation.requireAdmin(userId(jwt));
        return moderation.approve(id);
    }

    @PostMapping("/plants/{id}/reject")
    public PlantResponse reject(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                @jakarta.validation.Valid @RequestBody(required = false) RejectRequest request) {
        moderation.requireAdmin(userId(jwt));
        return moderation.reject(id, request == null ? null : request.note());
    }

    public record RejectRequest(@Size(max = 500) String note) {
    }
}
