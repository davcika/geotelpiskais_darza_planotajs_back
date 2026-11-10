package lv.darzaplanotajs.assistant;

import lv.darzaplanotajs.common.ApiErrors;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/** Vienīgā klase, kas zina par Groq, tāpēc citu modeli var pieslēgt, nomainot tikai šo */
@Component
public class GroqClient {

    private final RestClient http;
    private final String apiKey;
    private final String model;

    public GroqClient(@Value("${app.assistant.groq.api-key:}") String apiKey,
                      @Value("${app.assistant.groq.model}") String model,
                      @Value("${app.assistant.groq.base-url}") String baseUrl) {
        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(Duration.ofSeconds(60));
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public String chat(String system, List<ChatMessage> history) {
        if (apiKey.isBlank()) {
            throw ApiErrors.unavailable("Assistant is not configured (GROQ_API_KEY)");
        }
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", system));
        messages.addAll(history);
        try {
            CompletionResponse response = http.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(new CompletionRequest(model, messages, 0.4, 2048))
                    .retrieve()
                    .body(CompletionResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Assistant returned no answer");
            }
            return response.choices().getFirst().message().content();
        } catch (HttpClientErrorException.TooManyRequests e) {
            // Bezmaksas limits izsmelts
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Assistant is busy, try again in a minute", e);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Assistant is unavailable", e);
        }
    }

    record CompletionRequest(String model, List<ChatMessage> messages, double temperature, int max_tokens) {
    }

    record CompletionResponse(List<Choice> choices) {
    }

    record Choice(ChatMessage message) {
    }
}
