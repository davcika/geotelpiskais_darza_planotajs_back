package lv.darzaplanotajs.task;

/** Darba atkārtošanās veids */
public enum TaskKind {
    /** Vienreizējs darbs ar termiņu */
    ONCE,
    /** Ik pēc N dienām kopš pēdējās izpildes (piem. zāles pļaušana) */
    EVERY_N_DAYS,
    /** Katru gadu noteiktā laika logā, piem. koku balsināšana */
    YEARLY
}
