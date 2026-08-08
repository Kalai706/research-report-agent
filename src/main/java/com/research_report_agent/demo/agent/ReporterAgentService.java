package com.research_report_agent.demo.agent;

import com.research_report_agent.demo.config.AppConfiguration;
import com.research_report_agent.demo.config.ResearchGraphBuilder;
import com.research_report_agent.demo.state.ResearchState;
import com.research_report_agent.demo.tool.WebSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ReporterAgentService {

    private final VectorStore vectorStore;
    private final AppConfiguration appConfiguration;

    public ReporterAgentService(VectorStore vectorStore, AppConfiguration appConfiguration) {
        this.vectorStore = vectorStore;
        this.appConfiguration = appConfiguration;
    }

    public void generateReport(String userTopic) throws GraphStateException {

        ChatModel model;

        String openAiKey = requireEnv("OPENAI_API_KEY");
        String geminiApiKey = requireEnv("GEMINI_API_KEY");
        String tavilyKey = requireEnv("TAVILY_API_KEY");

        String aiProvider = appConfiguration.getProvider();

        String topic = !userTopic.isBlank()
                ? userTopic
                : "The environmental impact of large language model training";

        if(aiProvider.equals("openai")){
            model = openApiModel(openAiKey);
        }else {
            model = geminiModel(geminiApiKey);
        }

        WebSearchTool searchTool = new WebSearchTool(tavilyKey);
        ResearchGraphBuilder graphBuilder = new ResearchGraphBuilder(vectorStore);

        CompiledGraph<ResearchState> graph = graphBuilder.build(model, searchTool);

        System.out.println("=== Researching: " + topic + " ===\n");

        Optional<ResearchState> result = graph.invoke(Map.of(ResearchState.TOPIC, topic));

        result.flatMap(ResearchState::finalReport).ifPresentOrElse(
                report -> {
                    System.out.println("\n=== FINAL REPORT ===\n");
                    System.out.println(report);
                },
                () -> System.out.println("Graph finished without producing a final report.")
        );
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + name);
        }
        return value;
    }

    private static ChatModel openApiModel(String openAiKey){
        return OpenAiChatModel.builder()
                .apiKey(openAiKey)
                .modelName("gpt-4o")
                .temperature(0.3)
                .maxCompletionTokens(2048)
                .build();
    }

    private static ChatModel geminiModel(String geminiApiKey){
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.3)
                .maxOutputTokens(2048)
                .build();
    }

}
