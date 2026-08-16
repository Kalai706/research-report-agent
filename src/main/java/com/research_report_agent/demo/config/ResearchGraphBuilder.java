package com.research_report_agent.demo.config;

import com.research_report_agent.demo.node.*;
import com.research_report_agent.demo.state.ResearchState;
import com.research_report_agent.demo.tool.WebSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Component
public class ResearchGraphBuilder {

    private static final int MAX_REVISIONS = 2;
    private final VectorStore vectorStore;

    public ResearchGraphBuilder(VectorStore vectorStore ) {
        this.vectorStore = vectorStore;
    }

    public CompiledGraph<ResearchState> build(ChatModel model, WebSearchTool searchTool, MemorySaver checkpointSaver)
            throws GraphStateException {

        StateGraph<ResearchState> graph = new StateGraph<>(ResearchState.SCHEMA, ResearchState::new)
                .addNode("plan", node_async(new PlanNode(model)))
                .addNode("ragSearch", node_async(new RagSearchNode(this.vectorStore)))
                .addNode("search", node_async(new SearchNode(searchTool, 3)))
                .addNode("synthesize", node_async(new SynthesizeNode(model)))
                .addNode("critique", node_async(new CritiqueNode(model)))
                .addNode("humanReview", node_async(new HumanReviewNode()))
                .addNode("revise", node_async(new ReviseNode(model)))
                .addNode("finalize", node_async(new FinalizeNode()))


                .addEdge(START, "plan")
                .addEdge("plan", "ragSearch")
                .addConditionalEdges("ragSearch",
                        edge_async(ResearchGraphBuilder::routeAfterRagSearch),
                        Map.of(
                            "search","search",
                            "synthesize","synthesize"
                        ))
                .addEdge("search", "synthesize")
                .addEdge("synthesize", "critique")

                // The core agentic loop: critique routes to either "revise" (which
                // loops back to "critique") or "finalize" (which ends the graph).
                .addConditionalEdges(
                        "critique",
                        edge_async(ResearchGraphBuilder::routeAfterCritique),
                        Map.of(
                                "revise", "revise",
                                "humanReview", "humanReview"
                        )
                )
                .addEdge("revise", "critique")
                .addConditionalEdges("humanReview",
                        edge_async(ResearchGraphBuilder::routeAfterHumanReview),
                        Map.of(
                                "revise","revise",
                                "finalize","finalize"
                        )
                )
                .addEdge("finalize", END);

        CompileConfig compileConfig = CompileConfig.builder()
                .checkpointSaver(checkpointSaver)
                .interruptBefore("humanReview")
                .build();

        return graph.compile(compileConfig);
    }

    private static String routeAfterCritique(ResearchState state) {
        boolean approved = state.critique().startsWith("APPROVED");
        boolean outOfRevisions = state.iteration() >= MAX_REVISIONS;

        if (approved || outOfRevisions) {
            return "humanReview";
        }
        return "revise";
    }

    private static String routeAfterHumanReview(ResearchState state) {
        boolean approved = state.humanReview().startsWith("APPROVED");
        boolean outOfRevisions = state.iteration() >= MAX_REVISIONS;

        if (approved || outOfRevisions) {
            return "finalize";
        }
        return "revise";
    }

    private static String routeAfterRagSearch(ResearchState state) {
        boolean unansweredQuestions = state.unansweredQuestions() != null && !state.unansweredQuestions().isEmpty();
        if (unansweredQuestions) {
            return "search";
        }
        return "synthesize";
    }
}
