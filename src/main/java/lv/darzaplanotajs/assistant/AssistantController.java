package lv.darzaplanotajs.assistant;

import static lv.darzaplanotajs.common.CurrentUser.userId;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssistantController {

    private final AssistantService service;

    public AssistantController(AssistantService service) {
        this.service = service;
    }

    @PostMapping("/api/gardens/{gardenId}/assistant")
    public Map<String, String> ask(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                   @Valid @RequestBody AssistantRequest request) {
        return Map.of("answer", service.ask(userId(jwt), gardenId, request));
    }
}
