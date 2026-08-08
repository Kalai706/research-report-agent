package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import com.research_report_agent.demo.tool.WebSearchTool;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchNode implements NodeAction<ResearchState> {

    private final WebSearchTool searchTool;
    private final int resultsPerQuestion;

    public SearchNode(WebSearchTool searchTool, int resultsPerQuestion) {
        this.searchTool = searchTool;
        this.resultsPerQuestion = resultsPerQuestion;
    }

    @Override
    public Map<String, Object> apply(ResearchState state) {
        List<String> formatted = new ArrayList<>();

        for (String question : state.unansweredQuestions()) {
            List<WebSearchTool.Result> results = searchTool.search(question, resultsPerQuestion);
            System.out.println("[search] \"" + question + "\" -> " + results.size() + " results");

            for (WebSearchTool.Result r : results) {
                formatted.add("""
                        Sub-question: %s
                        Source: %s (%s)
                        Content: %s
                        ---""".formatted(question, r.title(), r.url(), r.content()));
            }
        }

        return Map.of(ResearchState.SEARCH_RESULTS, formatted);
    }
}
