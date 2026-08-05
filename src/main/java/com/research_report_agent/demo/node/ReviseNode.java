package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import dev.langchain4j.model.chat.ChatModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class ReviseNode implements NodeAction<ResearchState> {

    private final ChatModel model;

    public ReviseNode(ChatModel model) {
        this.model = model;
    }

    @Override
    public Map<String, Object> apply(ResearchState state) {
        String prompt = """
                Revise the draft report below based strictly on the editor's feedback.
                Keep everything that already works; only change what the feedback flags.
 
                EDITOR FEEDBACK:
                %s
 
                CURRENT DRAFT:
                %s
                """.formatted(state.critique(), state.draft());

        String revised = model.chat(prompt);
        System.out.println("[revise] produced revision #" + (state.iteration() + 1));

        return Map.of(
                ResearchState.DRAFT, revised,
                ResearchState.ITERATION, state.iteration() + 1
        );
    }
}
