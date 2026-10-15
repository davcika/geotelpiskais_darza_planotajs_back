package lv.darzaplanotajs.task;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import lv.darzaplanotajs.common.ApiErrors;
import lv.darzaplanotajs.garden.GardenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Piederību pārbauda caur dārzu, svešam darbam 404 */
@Service
@Transactional
public class TaskService {

    private static final Comparator<TaskResponse> ORDER = Comparator
            .comparing((TaskResponse t) -> !t.enabled())
            .thenComparing(TaskResponse::status)
            .thenComparing(TaskResponse::nextDue, Comparator.nullsLast(Comparator.naturalOrder()));

    private final GardenTaskRepository tasks;
    private final GardenService gardens;

    public TaskService(GardenTaskRepository tasks, GardenService gardens) {
        this.tasks = tasks;
        this.gardens = gardens;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findForGarden(long userId, long gardenId) {
        gardens.findOwnGarden(userId, gardenId);
        LocalDate today = TaskSchedule.today();
        return tasks.findByGardenIdOrderByIdAsc(gardenId).stream()
                .map(t -> TaskResponse.from(t, today)).sorted(ORDER).toList();
    }

    public TaskResponse create(long userId, long gardenId, TaskRequest request) {
        gardens.findOwnGarden(userId, gardenId);
        GardenTask task = new GardenTask(gardenId);
        apply(userId, task, request);
        return TaskResponse.from(tasks.save(task), TaskSchedule.today());
    }

    public TaskResponse update(long userId, long id, TaskRequest request) {
        GardenTask task = findOwn(userId, id);
        apply(userId, task, request);
        return TaskResponse.from(task, TaskSchedule.today());
    }

    public void delete(long userId, long id) {
        tasks.delete(findOwn(userId, id));
    }

    /** Atzīmē kā izdarītu šodien (vienreizējais darbs ar to ir pabeigts) */
    public TaskResponse done(long userId, long id) {
        GardenTask task = findOwn(userId, id);
        LocalDate today = TaskSchedule.today();
        task.markDone(today);
        return TaskResponse.from(task, today);
    }

    /** Ja nav ko atsaukt, atbild 400 */
    public TaskResponse undo(long userId, long id) {
        GardenTask task = findOwn(userId, id);
        LocalDate today = TaskSchedule.today();
        if (!task.undoDone(today)) throw ApiErrors.badRequest("Nothing to undo");
        return TaskResponse.from(task, today);
    }

    /** Darbus ar jau esošu nosaukumu nedublē */
    public List<TaskResponse> addDefaults(long userId, long gardenId) {
        return addDefaults(userId, gardenId, null);
    }

    @Transactional(readOnly = true)
    public List<DefaultTaskTemplate> listDefaults(long userId, long gardenId) {
        gardens.findOwnGarden(userId, gardenId);
        return defaults(gardenId).stream().map(t -> new DefaultTaskTemplate(t.title, t.note, t.kind, t.intervalDays,
                t.monthDayFrom, t.monthDayTo, tasks.existsByGardenIdAndIsDefaultTrueAndTitle(gardenId, t.title))).toList();
    }

    /** titles null nozīmē visus, un esošos nedublē */
    public List<TaskResponse> addDefaults(long userId, long gardenId, List<String> titles) {
        gardens.findOwnGarden(userId, gardenId);
        for (GardenTask task : defaults(gardenId)) {
            if (titles != null && !titles.contains(task.title)) continue;
            if (!tasks.existsByGardenIdAndIsDefaultTrueAndTitle(gardenId, task.title)) {
                tasks.save(task);
            }
        }
        return findForGarden(userId, gardenId);
    }

    static List<GardenTask> defaults(long gardenId) {
        GardenTask mow = new GardenTask(gardenId);
        mow.title = "Mow the lawn";
        mow.kind = TaskKind.EVERY_N_DAYS;
        mow.intervalDays = 7;
        mow.isDefault = true;
        return List.of(
                mow,
                GardenTask.yearly(gardenId, "Whitewash fruit trees", "02-15", "03-31", "Protects bark from sun scald and frost cracks"),
                GardenTask.yearly(gardenId, "Prune fruit trees", "03-01", "04-15", "Before buds open"),
                GardenTask.yearly(gardenId, "Trim the hedge (spring)", "04-01", "04-30", null),
                GardenTask.yearly(gardenId, "Fertilize the lawn", "05-01", "05-31", null),
                GardenTask.yearly(gardenId, "Trim the hedge (summer)", "08-01", "08-31", "After birds have finished nesting"),
                GardenTask.yearly(gardenId, "Plant spring bulbs", "09-15", "10-31", "Tulips, daffodils, crocuses"),
                GardenTask.yearly(gardenId, "Cover roses and rhododendrons for winter", "10-25", "11-15", null));
    }

    private void apply(long userId, GardenTask task, TaskRequest request) {
        switch (request.kind()) {
            case ONCE -> {
                if (request.dueDate() == null) throw ApiErrors.badRequest("One-off task needs a due date");
            }
            case EVERY_N_DAYS -> {
                if (request.intervalDays() == null) throw ApiErrors.badRequest("Repeating task needs an interval in days");
            }
            case YEARLY -> {
                if (request.monthDayFrom() == null || request.monthDayTo() == null) {
                    throw ApiErrors.badRequest("Yearly task needs a window (MM-DD to MM-DD)");
                }
                requireMonthDay(request.monthDayFrom());
                requireMonthDay(request.monthDayTo());
            }
        }
        if (request.objectId() != null) {
            if (!gardens.findOwnObject(userId, request.objectId()).getGardenId().equals(task.gardenId)) {
                throw ApiErrors.badRequest("Object belongs to another garden");
            }
        }
        task.update(request);
    }

    /** Regex pieļauj 31. februāri, tāpēc pārbauda īstu datumu */
    private static void requireMonthDay(String value) {
        try {
            MonthDay.parse("--" + value);
        } catch (DateTimeParseException e) {
            throw ApiErrors.badRequest("Invalid date " + value);
        }
    }

    private GardenTask findOwn(long userId, long id) {
        GardenTask task = tasks.findById(id).orElseThrow(() -> ApiErrors.notFound("Task", id));
        gardens.findOwnGarden(userId, task.gardenId); // svešam dārzam met 404
        return task;
    }
}
