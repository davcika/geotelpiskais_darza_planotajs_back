package lv.darzaplanotajs.task;

import java.util.List;

/** added nozīmē, ka darbs jau ir šajā dārzā */
public record DefaultTaskTemplate(String title, String note, TaskKind kind, Integer intervalDays,
                                  String monthDayFrom, String monthDayTo, boolean added) {

    /** Izvēlēto veidņu nosaukumi, null nozīmē visas */
    public record Selection(List<String> titles) {}
}
