package lv.darzaplanotajs.item;

import static lv.darzaplanotajs.common.CurrentUser.userId;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping("/items/types")
    public List<ItemType> types() {
        return service.types();
    }

    @GetMapping("/gardens/{gardenId}/items")
    public List<GardenItemResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.list(userId(jwt), gardenId);
    }

    @PostMapping("/gardens/{gardenId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public GardenItemResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                     @Valid @RequestBody GardenItemRequest request) {
        return service.create(userId(jwt), gardenId, request);
    }

    @PutMapping("/items/{id}")
    public GardenItemResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                                     @Valid @RequestBody GardenItemRequest request) {
        return service.update(userId(jwt), id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        service.delete(userId(jwt), id);
    }

    @GetMapping("/gardens/{gardenId}/items/effects")
    public Map<Long, ItemEffectResponse> effects(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                                 @RequestParam int year) {
        return service.effects(userId(jwt), gardenId, year);
    }
}
