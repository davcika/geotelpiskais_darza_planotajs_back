package lv.darzaplanotajs.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskScheduleTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    private static TaskSchedule.Due once(LocalDate due, LocalDate done) {
        return TaskSchedule.compute(TaskKind.ONCE, due, null, null, null, done, TODAY);
    }

    private static TaskSchedule.Due every(int days, LocalDate done) {
        return TaskSchedule.compute(TaskKind.EVERY_N_DAYS, null, days, null, null, done, TODAY);
    }

    private static TaskSchedule.Due yearly(String from, String to, LocalDate done) {
        return TaskSchedule.compute(TaskKind.YEARLY, null, null, from, to, done, TODAY);
    }

    @Test
    void oneOffStatusByDate() {
        assertEquals(TaskStatus.OVERDUE, once(TODAY.minusDays(1), null).status());
        assertEquals(TaskStatus.DUE, once(TODAY, null).status());
        assertEquals(TaskStatus.UPCOMING, once(TODAY.plusDays(14), null).status());
        assertEquals(TaskStatus.LATER, once(TODAY.plusDays(15), null).status());
    }

    @Test
    void oneOffDoneIsFinished() {
        TaskSchedule.Due due = once(TODAY.minusDays(3), TODAY.minusDays(1));
        assertEquals(TaskStatus.DONE, due.status());
        assertNull(due.nextDue());
    }

    @Test
    void repeatingCountsFromLastDone() {
        assertEquals(new TaskSchedule.Due(TODAY, TaskStatus.DUE), every(7, null));
        assertEquals(TaskStatus.OVERDUE, every(7, TODAY.minusDays(9)).status());
        assertEquals(new TaskSchedule.Due(TODAY.plusDays(5), TaskStatus.UPCOMING), every(7, TODAY.minusDays(2)));
    }

    @Test
    void yearlyInsideWindow() {
        assertEquals(new TaskSchedule.Due(LocalDate.of(2026, 2, 15), TaskStatus.DUE), yearly("02-15", "03-31", null));
        // izdarīts pagājušajā gadā, tāpēc joprojām jādara
        assertEquals(TaskStatus.DUE, yearly("02-15", "03-31", LocalDate.of(2025, 3, 1)).status());
    }

    @Test
    void yearlyDoneMovesToNextYear() {
        assertEquals(new TaskSchedule.Due(LocalDate.of(2027, 2, 15), TaskStatus.LATER),
                yearly("02-15", "03-31", LocalDate.of(2026, 2, 20)));
    }

    @Test
    void yearlyBeforeAndAfterWindow() {
        assertEquals(new TaskSchedule.Due(LocalDate.of(2026, 3, 20), TaskStatus.UPCOMING), yearly("03-20", "04-15", null));
        assertEquals(new TaskSchedule.Due(LocalDate.of(2026, 8, 1), TaskStatus.LATER), yearly("08-01", "08-31", null));
        // nokavēts logs pāriet uz nākamo gadu
        assertEquals(LocalDate.of(2027, 1, 10), yearly("01-10", "02-20", null).nextDue());
    }

    @Test
    void yearlyWindowAcrossNewYear() {
        // logs no novembra līdz martam ietver šodienu
        assertEquals(new TaskSchedule.Due(LocalDate.of(2025, 11, 15), TaskStatus.DUE), yearly("11-15", "03-20", null));
    }
}
