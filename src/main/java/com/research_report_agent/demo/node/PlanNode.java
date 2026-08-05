package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import dev.langchain4j.model.chat.ChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlanNode implements NodeAction<ResearchState> {

    private final ChatModel model;

    public PlanNode(ChatModel model){
        this.model = model;
    }


    @Override
    public Map<String, Object> apply(ResearchState state) throws Exception {
        // split the question into subquestion
        String prompt = """
                You are a research planner. Break the following topic into 3 to 5
                specific, independently-searchable sub-questions that together would
                let someone write a well-rounded report on it.

                Topic: %s

                Respond with ONLY the questions, one per line, no numbering, no preamble.
                """.formatted(state.topic());
        String response = model.chat(prompt);
        List<String> questions = Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        System.out.println("[plan] generated " + questions.size() + " sub-questions");

        return Map.of(ResearchState.SUB_QUESTIONS,questions);
    }

}
