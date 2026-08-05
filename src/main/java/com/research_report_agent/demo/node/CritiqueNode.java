package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import dev.langchain4j.model.chat.ChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class CritiqueNode implements NodeAction<ResearchState> {


    private final ChatModel model;

    public CritiqueNode(ChatModel model) {
        this.model = model;
    }

    @Override
    public Map<String, Object> apply(ResearchState state) {
        String prompt = """
                You are a strict editor reviewing a research draft on: %s
 
                Original sub-questions the report was supposed to answer:
                %s
 
                DRAFT:
                %s
 
                If the draft adequately covers the sub-questions, is well-cited, and
                has no unsupported claims, respond with exactly:
                APPROVED
 
                Otherwise respond starting with:
                REVISE: <specific, actionable feedback on what to fix>
                """.formatted(state.topic(), String.join("\n", state.subQuestions()), state.draft());

        String critique = model.chat(prompt).trim();
        System.out.println("[critique] iteration " + state.iteration() + " -> "
                + (critique.startsWith("APPROVED") ? "APPROVED" : "NEEDS REVISION"));

        return Map.of(ResearchState.CRITIQUE, critique);
    }
}
