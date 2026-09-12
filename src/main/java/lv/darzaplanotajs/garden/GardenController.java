package lv.darzaplanotajs.garden;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GardenController {

    private final GardenService service;
    private final FrostService frost;

    public GardenController(GardenService service, FrostService frost) {
        this.service = service;
        this.frost = frost;
    }

    @GetMapping("/gardens")
    public List<GardenResponse> gardens(@AuthenticationPrincipal Jwt jwt) {
        return service.findGardens(userId(jwt));
    }

    @PostMapping("/gardens")
    @ResponseStatus(HttpStatus.CREATED)
    public GardenResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody GardenRequest request) {
        return service.createGarden(userId(jwt), request);
    }

    @PutMapping("/gardens/{gardenId}")
    public GardenResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                 @Valid @RequestBody GardenRequest request) {
        return service.updateGarden(userId(jwt), gardenId, request);
    }

    @DeleteMapping("/gardens/{gardenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        service.deleteGarden(userId(jwt), gardenId);
    }

    @PostMapping("/gardens/{gardenId}/frost/compute")
    public FrostResponse computeFrost(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                      @Valid @RequestBody FrostComputeRequest request) {
        return frost.compute(userId(jwt), gardenId, request);
    }

    @PutMapping("/gardens/{gardenId}/frost")
    public FrostResponse setFrost(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                  @Valid @RequestBody FrostRequest request) {
        return frost.setManual(userId(jwt), gardenId, request);
    }

    @PutMapping("/gardens/{gardenId}/hub")
    public GardenResponse setHub(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                 @Valid @RequestBody HubRequest request) {
        return service.setHub(userId(jwt), gardenId, request);
    }

    @GetMapping("/gardens/{gardenId}/objects")
    public List<GardenObjectResponse> objects(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.findObjects(userId(jwt), gardenId);
    }

    @PostMapping("/gardens/{gardenId}/objects")
    @ResponseStatus(HttpStatus.CREATED)
    public GardenObjectResponse createObject(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                             @Valid @RequestBody GardenObjectRequest request) {
        return service.createObject(userId(jwt), gardenId, request);
    }

    @PutMapping("/objects/{id}")
    public GardenObjectResponse updateObject(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                             @Valid @RequestBody GardenObjectRequest request) {
        return service.updateObject(userId(jwt), id, request);
    }

    @DeleteMapping("/objects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteObject(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        service.deleteObject(userId(jwt), id);
    }

    /** Tokena sub lauks ir lietotāja id */
}
