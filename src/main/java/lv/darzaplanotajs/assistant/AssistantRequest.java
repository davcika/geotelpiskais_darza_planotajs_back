package lv.darzaplanotajs.assistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Visa čata vēsture, jo serveris neko neglabā */
public record AssistantRequest(@NotEmpty @Size(max = 20) List<@Valid ChatMessage> messages, int year) {
}
