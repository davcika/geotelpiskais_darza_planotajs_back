package lv.darzaplanotajs.task;

import java.time.LocalDate;

/** Darbs ar aprēķināto nākamo termiņu un stāvokli */
public record TaskResponse(long id, Long objectId, String title, String note, TaskKind kind,
                           LocalDate dueDate, Integer intervalDays, String monthDayFrom, String monthDayTo,
                           LocalDate lastDoneOn, boolean enabled, boolean isDefault,
                           LocalDate nextDue, TaskStatus status) {

    static TaskResponse from(GardenTask t, LocalDate today) {
        TaskSchedule.Due due = t.due(today);
        return new TaskResponse(t.id, t.objectId, t.title, t.note, t.kind, t.dueDate, t.intervalDays,
                t.monthDayFrom, t.monthDayTo, t.lastDoneOn, t.enabled, t.isDefault, due.nextDue(), due.status());
    }
}
