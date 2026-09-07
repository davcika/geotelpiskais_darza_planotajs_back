package lv.darzaplanotajs.address;

import lv.darzaplanotajs.common.ApiErrors;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private static final int MIN_QUERY_LENGTH = 3;
    private static final int MAX_QUERY_LENGTH = 100;
    private static final int MAX_LIMIT = 20;

    private final AddressSearch search;

    public AddressController(AddressSearch search) {
        this.search = search;
    }

    @GetMapping
    public List<AddressResponse> search(@RequestParam String q, @RequestParam(defaultValue = "8") int limit) {
        int length = q.trim().length();
        if (length < MIN_QUERY_LENGTH || length > MAX_QUERY_LENGTH || limit < 1 || limit > MAX_LIMIT) {
            throw ApiErrors.badRequest("Query must have %d-%d characters and limit must be 1-%d"
                    .formatted(MIN_QUERY_LENGTH, MAX_QUERY_LENGTH, MAX_LIMIT));
        }
        if (!search.isReady()) {
            throw ApiErrors.unavailable("Address data is being loaded");
        }
        return search.search(q, limit);
    }
}
