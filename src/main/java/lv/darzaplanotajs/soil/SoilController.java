package lv.darzaplanotajs.soil;

import static lv.darzaplanotajs.common.CurrentUser.userId;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
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
public class SoilController {

    private final SoilService service;

    public SoilController(SoilService service) {
        this.service = service;
    }

    @GetMapping("/gardens/{gardenId}/soil")
    public SoilResponse getGarden(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.getGarden(userId(jwt), gardenId);
    }

    @PutMapping("/gardens/{gardenId}/soil")
    public SoilResponse putGarden(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                  @Valid @RequestBody SoilRequest request) {
        return service.putGarden(userId(jwt), gardenId, request);
    }

    @GetMapping("/objects/{objectId}/soil")
    public BedSoilResponse getBed(@AuthenticationPrincipal Jwt jwt, @PathVariable long objectId) {
        return service.getBed(userId(jwt), objectId);
    }

    @PutMapping("/objects/{objectId}/soil")
    public BedSoilResponse putBed(@AuthenticationPrincipal Jwt jwt, @PathVariable long objectId,
                                  @Valid @RequestBody SoilRequest request) {
        return service.putBed(userId(jwt), objectId, request);
    }

    @DeleteMapping("/objects/{objectId}/soil")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBed(@AuthenticationPrincipal Jwt jwt, @PathVariable long objectId) {
        service.deleteBed(userId(jwt), objectId);
    }

    @GetMapping("/objects/{objectId}/soil/suitability")
    public SuitabilityResponse suitability(@AuthenticationPrincipal Jwt jwt, @PathVariable long objectId,
                                           @RequestParam(required = false) Integer year) {
        return service.suitability(userId(jwt), objectId, yearOrNow(year));
    }

    @PostMapping("/objects/{objectId}/soil/advice")
    public AdviceResponse advice(@AuthenticationPrincipal Jwt jwt, @PathVariable long objectId,
                                 @RequestParam(required = false) Integer year) {
        return service.advice(userId(jwt), objectId, yearOrNow(year));
    }

    private static int yearOrNow(Integer year) {
        return year != null ? year : LocalDate.now(ZoneId.of("Europe/Riga")).getYear();
    }
}
