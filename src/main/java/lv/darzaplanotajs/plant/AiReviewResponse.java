package lv.darzaplanotajs.plant;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** verdict ir null, ja AI nav pieejams vai atbildi nevarēja saprast */
public record AiReviewResponse(boolean available, String verdict, List<String> reasons, String error) {

    private static final Pattern VERDICT = Pattern.compile("VERDICT:\\s*(APPROVE|FIX|REJECT)", Pattern.CASE_INSENSITIVE);

    static AiReviewResponse parse(String answer) {
        Matcher m = VERDICT.matcher(answer);
        String verdict = m.find() ? m.group(1).toUpperCase(java.util.Locale.ROOT) : null;
        List<String> reasons = answer.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !VERDICT.matcher(line).find())
                .map(line -> line.replaceFirst("^[-*•]\\s*", ""))
                .limit(8)
                .toList();
        return new AiReviewResponse(true, verdict, reasons, null);
    }

    static AiReviewResponse unavailable(String error) {
        return new AiReviewResponse(false, null, List.of(), error);
    }
}
