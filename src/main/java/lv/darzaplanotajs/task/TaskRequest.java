package lv.darzaplanotajs.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** kind nosaka, kuri lauki ir obligāti, to pārbauda TaskService */
public record TaskRequest(
        Long objectId,
        @NotBlank @Size(max = 100) String title,
        @Size(max = 300) String note,
        @NotNull TaskKind kind,
        LocalDate dueDate,
        @Min(1) @Max(3650) Integer intervalDays,
        @Pattern(regexp = "(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])") String monthDayFrom,
        @Pattern(regexp = "(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])") String monthDayTo,
        LocalDate lastDoneOn,
        Boolean enabled) {
}
