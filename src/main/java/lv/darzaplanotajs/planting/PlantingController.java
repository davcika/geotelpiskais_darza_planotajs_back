package lv.darzaplanotajs.planting;

import static lv.darzaplanotajs.common.CurrentUser.userId;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PlantingController {

    private final PlantingService service;

    public PlantingController(PlantingService service) {
        this.service = service;
    }

    @GetMapping("/gardens/{gardenId}/plantings")
    public List<PlantingResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                       @RequestParam int year) {
        return service.findForGarden(userId(jwt), gardenId, year);
    }

    @PostMapping("/objects/{objectId}/plantings")
    @ResponseStatus(HttpStatus.CREATED)
    public PlantingResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable long objectId,
                                   @Valid @RequestBody PlantingRequest request) {
        return service.create(userId(jwt), objectId, request);
    }

    @PutMapping("/plantings/{id}")
    public PlantingResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                   @Valid @RequestBody PlantingRequest request) {
        return service.update(userId(jwt), id, request);
    }

    @DeleteMapping("/plantings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        service.delete(userId(jwt), id);
    }
}
