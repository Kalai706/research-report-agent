package com.research_report_agent.demo.config;

import com.research_report_agent.demo.node.*;
import com.research_report_agent.demo.state.ResearchState;
import com.research_report_agent.demo.tool.WebSearchTool;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

public class ResearchGraphBuilder {

    private static final int MAX_REVISIONS = 3;

    public static CompiledGraph<ResearchState> build(OpenAiChatModel model, WebSearchTool searchTool)
            throws GraphStateException {

        StateGraph<ResearchState> graph = new StateGraph<>(ResearchState.SCHEMA, ResearchState::new)
                .addNode("plan", node_async(new PlanNode(model)))
                .addNode("search", node_async(new SearchNode(searchTool, 3)))
                .addNode("synthesize", node_async(new SynthesizeNode(model)))
                .addNode("critique", node_async(new CritiqueNode(model)))
                .addNode("revise", node_async(new ReviseNode(model)))
                .addNode("finalize", node_async(new FinalizeNode()))

                .addEdge(START, "plan")
                .addEdge("plan", "search")
                .addEdge("search", "synthesize")
                .addEdge("synthesize", "critique")

                // The core agentic loop: critique routes to either "revise" (which
                // loops back to "critique") or "finalize" (which ends the graph).
                .addConditionalEdges(
                        "critique",
                        edge_async(ResearchGraphBuilder::routeAfterCritique),
                        Map.of(
                                "revise", "revise",
                                "finalize", "finalize"
                        )
                )
                .addEdge("revise", "critique")
                .addEdge("finalize", END);

        return graph.compile();
    }

    private static String routeAfterCritique(ResearchState state) {
        boolean approved = state.critique().startsWith("APPROVED");
        boolean outOfRevisions = state.iteration() >= MAX_REVISIONS;

        if (approved || outOfRevisions) {
            return "finalize";
        }
        return "revise";
    }
}
