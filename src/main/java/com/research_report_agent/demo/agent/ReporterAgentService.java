package com.research_report_agent.demo.agent;

import com.research_report_agent.demo.config.AppConfiguration;
import com.research_report_agent.demo.config.ResearchGraphBuilder;
import com.research_report_agent.demo.state.ResearchState;
import com.research_report_agent.demo.tool.WebSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

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

        MemorySaver checkpointSaver = new MemorySaver();


        // The graph now always pauses before "humanReview" - see
        // ResearchGraphBuilder for the interruptBefore("humanReview") wiring.
        CompiledGraph<ResearchState> graph = graphBuilder.build(
                model, searchTool, checkpointSaver
        );

        // thread_id names this session. Reusing it is what lets LangGraph4j
        // reload the right checkpoint - a new topic should generally get a
        // fresh thread_id, the same way a new chat conversation would.
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("research-session-" + System.currentTimeMillis())
                .build();

        System.out.println("=== Researching: " + topic + " ===\n");

        // First call: runs plan -> retrieve -> [search] -> synthesize ->
        // critique <-> revise (self-critique loop), then PAUSES before
        // humanReview. Every subsequent call in the loop below also pauses
        // there again if the human rejects and a revision is needed.
        Optional<ResearchState> current = graph.invoke(Map.of(ResearchState.TOPIC, topic), runnableConfig);
        Scanner scanner = new Scanner(System.in);


        while (current.isPresent() && current.get().finalReport().get().isBlank()) {
            ResearchState state = current.get();

            System.out.println("\n--- Human review (thread: " + runnableConfig.threadId().orElse("?") + ") ---");
            System.out.println("Revisions so far: " + state.iteration());
            System.out.println("\nDraft report:\n" + state.draft());
            System.out.println("\nSelf-critique verdict:\n" + state.critique());

            System.out.print("\nApprove this draft? (y = approve / n = reject with feedback): ");
            String answer = scanner.nextLine().trim();

            Map<String, Object> resumeValues;
            if (answer.equalsIgnoreCase("y")) {
                resumeValues = Map.of(ResearchState.HUMAN_REVIEW, "APPROVED");
            } else {
                System.out.print("What should be fixed? ");
                String feedback = scanner.nextLine().trim();
                // Reuses ReviseNode's existing contract: it reads CRITIQUE and
                // revises the draft accordingly, regardless of whether that
                // critique came from the LLM or a person.
                resumeValues = Map.of(
                        ResearchState.HUMAN_REVIEW, "REJECTED",
                        ResearchState.CRITIQUE, "REVISE: " + feedback
                );
            }

            // GraphInput.resume(Map) both injects these values into state AND
            // tells LangGraph4j to continue from the checkpoint for this
            // thread_id - the graph picks up right at "humanReview" with the
            // human's decision now visible to routeAfterHumanReview.
            current = graph.invoke(GraphInput.resume(resumeValues), runnableConfig);
        }

        current.flatMap(ResearchState::finalReport).ifPresentOrElse(
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
