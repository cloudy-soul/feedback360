package com.feedback.feedback360.services.talentup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

@Component
public class TalentUpClient {

    private final RestClient restClient;

    public TalentUpClient(@Value("${app.talentup.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        // When you have real TalentUp credentials, add here:
        // .defaultHeader("Authorization", "Bearer " + apiKey)
    }

    public List<TalentUpCompletionDto> fetchCompletions(String sinceIso) {
        TalentUpCompletionsResponse response = restClient.get()
                .uri(b -> b.path("/api/completions").queryParam("since", sinceIso).build())
                .retrieve()
                .body(TalentUpCompletionsResponse.class);
        return response != null ? response.completions() : List.of();
    }
}