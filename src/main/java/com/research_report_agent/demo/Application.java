package com.research_report_agent.demo;

import com.research_report_agent.demo.config.ResearchGraphBuilder;
import com.research_report_agent.demo.state.ResearchState;
import com.research_report_agent.demo.tool.WebSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws Exception {
		String openAiKey = requireEnv("OPENAI_API_KEY");
		String geminiApiKey = requireEnv("GEMINI_API_KEY");
		String tavilyKey = requireEnv("TAVILY_API_KEY");

		String topic = args.length > 0
				? String.join(" ", args)
				: "The environmental impact of large language model training";

//		ChatModel model = OpenAiChatModel.builder()
		ChatModel model = GoogleAiGeminiChatModel.builder()
//				.apiKey(openAiKey)
				.apiKey(geminiApiKey)
				.modelName("gemini-3.5-flash-lite")
				.temperature(0.3)
				.maxOutputTokens(2048)
				.build();

		WebSearchTool searchTool = new WebSearchTool(tavilyKey);

		CompiledGraph<ResearchState> graph = ResearchGraphBuilder.build(model, searchTool);

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

}
