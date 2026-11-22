package lv.darzaplanotajs.garden;

import static lv.darzaplanotajs.common.CurrentUser.userId;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SharingController {

    private final SharingService service;

    public SharingController(SharingService service) {
        this.service = service;
    }

    @PostMapping("/gardens/{gardenId}/invite")
    public SharingService.InviteResponse createInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.createInvite(userId(jwt), gardenId);
    }

    /** 204, ja aktīvas saites nav */
    @GetMapping("/gardens/{gardenId}/invite")
    public org.springframework.http.ResponseEntity<SharingService.InviteResponse> currentInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.currentInvite(userId(jwt), gardenId)
                .map(org.springframework.http.ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/gardens/{gardenId}/invite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvite(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        service.revokeInvite(userId(jwt), gardenId);
    }

    @GetMapping("/gardens/{gardenId}/members")
    public List<SharingService.MemberResponse> members(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.members(userId(jwt), gardenId);
    }

    @DeleteMapping("/gardens/{gardenId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                             @PathVariable long memberId) {
        service.removeMember(userId(jwt), gardenId, memberId);
    }

    /** Uzaicinājuma priekšskatījums, frontend pirms tam aizved uz login */
    @GetMapping("/invites/{token}")
    public SharingService.InvitePreview preview(@AuthenticationPrincipal Jwt jwt, @PathVariable String token) {
        return service.preview(userId(jwt), token);
    }

    @PostMapping("/invites/{token}/accept")
    public GardenResponse accept(@AuthenticationPrincipal Jwt jwt, @PathVariable String token) {
        return service.accept(userId(jwt), token);
    }

    /** Tokena sub lauks ir lietotāja id */
}
