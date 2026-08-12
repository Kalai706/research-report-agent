package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class RagSearchNode implements NodeAction<ResearchState> {

    public static final double THRESHOLD = 0.05;

    private final VectorStore vectorStore;

    public RagSearchNode(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public Map<String, Object> apply(ResearchState state) {
        List<String> searchResult = new ArrayList<>();
        List<String> unansweredQuestions = new ArrayList<>();

//       TODO: Update with Conversational RAG
//        FilterExpressionBuilder fb = new FilterExpressionBuilder();
//        fb.and(
//                fb.eq("source", "ai-food-research-2-5.pdf"),
//                fb.
//
//        )


        for(String question : state.subQuestions()){
            List<Document> ragResult = vectorStore.similaritySearch(SearchRequest.builder()
                    .query(question)
                    .topK(3)
                    .similarityThreshold(0.5)
//                            .filterExpression("source:research_report_agent")
                    .build());
            if (ragResult.isEmpty()) {
                log.warn("No documents met similarity threshold {} for query: {}", THRESHOLD, question);
                unansweredQuestions.add(question);
            } else {
                log.info("RAG match found with threshold {} for query: {}",THRESHOLD, question);
                System.out.println("[RAG SOURCE] :" + ragResult.stream().map(Document::getText).toList());
                searchResult.addAll(ragResult.stream().map(Document::getText).toList());
            }
        }

        return Map.of(ResearchState.SEARCH_RESULTS, searchResult,
                ResearchState.UNANSWERED_QUESTIONS, unansweredQuestions);
    }


}
