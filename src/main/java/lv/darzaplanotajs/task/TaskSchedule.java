package lv.darzaplanotajs.task;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;

/** Tīrs termiņa aprēķins (bez DB), lai to varētu viegli testēt */
public final class TaskSchedule {

    public static final ZoneId RIGA = ZoneId.of("Europe/Riga");
    /** Cik dienas uz priekšu darbs skaitās drīzumā */
    static final int UPCOMING_DAYS = 14;

    private TaskSchedule() {
    }

    /** nextDue = null nozīmē, ka vienreizējais darbs jau izpildīts */
    public record Due(LocalDate nextDue, TaskStatus status) {
    }

    public static LocalDate today() {
        return LocalDate.now(RIGA);
    }

    public static Due compute(TaskKind kind, LocalDate dueDate, Integer intervalDays,
                              String from, String to, LocalDate lastDoneOn, LocalDate today) {
        return switch (kind) {
            case ONCE -> lastDoneOn != null ? new Due(null, TaskStatus.DONE) : byDate(dueDate, today);
            case EVERY_N_DAYS -> byDate(lastDoneOn == null ? today : lastDoneOn.plusDays(intervalDays), today);
            case YEARLY -> yearly(MonthDay.parse("--" + from), MonthDay.parse("--" + to), lastDoneOn, today);
        };
    }

    private static Due byDate(LocalDate due, LocalDate today) {
        TaskStatus status;
        if (due.isBefore(today)) status = TaskStatus.OVERDUE;
        else if (due.isEqual(today)) status = TaskStatus.DUE;
        else if (!due.isAfter(today.plusDays(UPCOMING_DAYS))) status = TaskStatus.UPCOMING;
        else status = TaskStatus.LATER;
        return new Due(due, status);
    }

    /**
     * Ņem pirmo logu, kas vēl nav beidzies un kurā darbs nav izdarīts, tāpēc nokavēts logs pāriet uz nākamo gadu.
     * Logs var iet arī pāri gadu mijai.
     */
    private static Due yearly(MonthDay from, MonthDay to, LocalDate lastDoneOn, LocalDate today) {
        boolean wraps = to.isBefore(from);
        for (int year = today.getYear() - 1; ; year++) {
            LocalDate start = from.atYear(year);
            LocalDate end = to.atYear(wraps ? year + 1 : year);
            if (end.isBefore(today)) continue;
            if (lastDoneOn != null && !lastDoneOn.isBefore(start)) continue; // šajā logā jau izdarīts
            if (!start.isAfter(today)) return new Due(start, TaskStatus.DUE);
            return byDate(start, today);
        }
    }
}
