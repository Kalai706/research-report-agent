package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class HumanReviewNode implements NodeAction<ResearchState> {
    @Override
    public Map<String, Object> apply(ResearchState state) throws Exception {
        System.out.println("[human-review] resuming with decision=" + state.humanReview());
        return Map.of();
    }
}
