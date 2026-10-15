package lv.darzaplanotajs.task;

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
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/gardens/{gardenId}/tasks")
    public List<TaskResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.findForGarden(userId(jwt), gardenId);
    }

    @PostMapping("/gardens/{gardenId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                               @Valid @RequestBody TaskRequest request) {
        return service.create(userId(jwt), gardenId, request);
    }

    @PostMapping("/gardens/{gardenId}/tasks/defaults")
    public List<TaskResponse> addDefaults(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId,
                                          @RequestBody(required = false) DefaultTaskTemplate.Selection selection) {
        return service.addDefaults(userId(jwt), gardenId, selection == null ? null : selection.titles());
    }

    @GetMapping("/gardens/{gardenId}/tasks/defaults")
    public List<DefaultTaskTemplate> listDefaults(@AuthenticationPrincipal Jwt jwt, @PathVariable long gardenId) {
        return service.listDefaults(userId(jwt), gardenId);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                               @Valid @RequestBody TaskRequest request) {
        return service.update(userId(jwt), id, request);
    }

    @PostMapping("/tasks/{id}/done")
    public TaskResponse done(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return service.done(userId(jwt), id);
    }

    @PostMapping("/tasks/{id}/undo")
    public TaskResponse undo(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return service.undo(userId(jwt), id);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        service.delete(userId(jwt), id);
    }
}
