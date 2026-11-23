package lv.darzaplanotajs.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Done atsaukšana atjauno iepriekšējo last_done_on */
class TaskUndoTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 20);

    @Test
    void undoRestoresPreviousDate() {
        GardenTask task = new GardenTask(1L);
        task.lastDoneOn = LocalDate.of(2026, 5, 13);
        task.markDone(TODAY);
        assertThat(task.lastDoneOn).isEqualTo(TODAY);
        assertThat(task.undoDone(TODAY)).isTrue();
        assertThat(task.lastDoneOn).isEqualTo(LocalDate.of(2026, 5, 13));
        assertThat(task.previousDoneOn).isNull();
    }

    @Test
    void undoOfFirstEverDoneClearsDate() {
        GardenTask task = new GardenTask(1L);
        task.markDone(TODAY);
        assertThat(task.undoDone(TODAY)).isTrue();
        assertThat(task.lastDoneOn).isNull();
    }

    @Test
    void cannotUndoOldOrMissingDone() {
        GardenTask never = new GardenTask(1L);
        assertThat(never.undoDone(TODAY)).isFalse();

        GardenTask old = new GardenTask(1L);
        old.markDone(TODAY.minusDays(1));
        assertThat(old.undoDone(TODAY)).isFalse();
        assertThat(old.lastDoneOn).isEqualTo(TODAY.minusDays(1));
    }
}
