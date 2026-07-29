package com.research_report_agent.demo.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebSearchTool {


    private record SearchRequest(String api_key, String query, int max_results) {}
    public record Result(String title, String url, String content) {}

    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<Result> search(String query, int maxResults) {
        try {
            String body = mapper.writeValueAsString(new SearchRequest(apiKey, query, maxResults));
            Request request = new Request.Builder()
                    .url("https://api.tavily.com/search")
                    .post(RequestBody.create(body, okhttp3.MediaType.get("application/json")))
                    .build();

            try (okhttp3.Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Tavily search failed: " + response);
                }
                JsonNode root = mapper.readTree(response.body().string());
                List<Result> results = new ArrayList<>();
                for (JsonNode r : root.path("results")) {
                    results.add(new Result(
                            r.path("title").asText(""),
                            r.path("url").asText(""),
                            r.path("content").asText("")
                    ));
                }
                return results;
            }
        } catch (IOException e) {
            throw new RuntimeException("Web search failed for query: " + query, e);
        }
    }

}
