package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class SynthesizeNode implements NodeAction<ResearchState> {

    private final OpenAiChatModel model;

    public SynthesizeNode(OpenAiChatModel model) {
        this.model = model;
    }

    @Override
    public Map<String, Object> apply(ResearchState state) {
        String sources = String.join("\n\n", state.searchResults());

        String prompt = """
                You are a research analyst. Using ONLY the source material below,
                write a well-structured report on: %s
 
                Rules:
                - Use headings for each major sub-topic.
                - Cite sources inline like [1], [2] matching a "Sources" list at the end.
                - Do not state anything not supported by the material below.
                - If the material is thin on a point, say so rather than inventing detail.
 
                SOURCE MATERIAL:
                %s
                """.formatted(state.topic(), sources);

        String draft = model.chat(prompt);
        System.out.println("[synthesize] draft length=" + draft.length() + " chars");

        return Map.of(ResearchState.DRAFT, draft);
    }
}
