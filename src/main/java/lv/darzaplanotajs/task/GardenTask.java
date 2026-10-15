package lv.darzaplanotajs.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;

/** Dārza darbs vai atgādinājums */
@Entity
public class GardenTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long gardenId;

    Long objectId;

    @Column(nullable = false)
    String title;

    String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TaskKind kind;

    LocalDate dueDate;
    Integer intervalDays;
    String monthDayFrom;
    String monthDayTo;
    LocalDate lastDoneOn;
    /** Iepriekšējā vērtība, lai pēdējo Done varētu atsaukt */
    LocalDate previousDoneOn;

    @Column(nullable = false)
    boolean enabled = true;

    @Column(nullable = false)
    boolean isDefault;

    @Column(nullable = false, insertable = false, updatable = false)
    Instant createdAt;

    protected GardenTask() {
    }

    GardenTask(Long gardenId) {
        this.gardenId = gardenId;
    }

    /** Ieteiktais darbs dārza līmenī */
    static GardenTask yearly(Long gardenId, String title, String from, String to, String note) {
        GardenTask task = new GardenTask(gardenId);
        task.title = title;
        task.note = note;
        task.kind = TaskKind.YEARLY;
        task.monthDayFrom = from;
        task.monthDayTo = to;
        task.isDefault = true;
        return task;
    }

    void update(TaskRequest request) {
        this.objectId = request.objectId();
        this.title = request.title().trim();
        this.note = request.note();
        this.kind = request.kind();
        this.dueDate = kind == TaskKind.ONCE ? request.dueDate() : null;
        this.intervalDays = kind == TaskKind.EVERY_N_DAYS ? request.intervalDays() : null;
        this.monthDayFrom = kind == TaskKind.YEARLY ? request.monthDayFrom() : null;
        this.monthDayTo = kind == TaskKind.YEARLY ? request.monthDayTo() : null;
        this.enabled = request.enabled() == null || request.enabled();
        if (request.lastDoneOn() != null) this.lastDoneOn = request.lastDoneOn();
    }

    /** Iepriekšējo vērtību saglabā, lai varētu atsaukt */
    void markDone(LocalDate today) {
        this.previousDoneOn = this.lastDoneOn;
        this.lastDoneOn = today;
    }

    /** Atsaukt var tikai šodien izdarīto, citādi atgriež false un neko nemaina */
    boolean undoDone(LocalDate today) {
        if (lastDoneOn == null || !lastDoneOn.equals(today)) return false;
        this.lastDoneOn = this.previousDoneOn;
        this.previousDoneOn = null;
        return true;
    }

    TaskSchedule.Due due(LocalDate today) {
        return TaskSchedule.compute(kind, dueDate, intervalDays, monthDayFrom, monthDayTo, lastDoneOn, today);
    }
}
