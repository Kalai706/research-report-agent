package com.research_report_agent.demo.node;

import com.research_report_agent.demo.state.ResearchState;
import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

public class FinalizeNode implements NodeAction<ResearchState> {
    @Override
    public Map<String, Object> apply(ResearchState state) {
        System.out.println("[finalize] report complete after " + state.iteration() + " revision(s)");
        return Map.of(ResearchState.FINAL_REPORT, state.draft());
    }
}
