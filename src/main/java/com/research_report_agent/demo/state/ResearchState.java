package com.research_report_agent.demo.state;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResearchState extends AgentState {

    public static final String TOPIC = "topic";
    public static final String SUB_QUESTIONS = "subQuestions";
    public static final String SEARCH_RESULTS = "searchResults";
    public static final String DRAFT = "draft";
    public static final String CRITIQUE = "critique";
    public static final String ITERATION = "iteration";
    public static final String FINAL_REPORT = "finalReport";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            TOPIC, Channels.base(() -> ""),
            SUB_QUESTIONS, Channels.appender(ArrayList::new),
            SEARCH_RESULTS, Channels.appender(ArrayList::new),
            DRAFT, Channels.base(() -> ""),
            CRITIQUE, Channels.base(() -> ""),
            ITERATION, Channels.base(() -> 0),
            FINAL_REPORT, Channels.base(() -> "")
    );

    public ResearchState(Map<String, Object> initData) {
        super(initData);
    }

    public String topic() {
        return this.<String>value(TOPIC).orElse("");
    }

    @SuppressWarnings("unchecked")
    public List<String> subQuestions() {
        return (List<String>) this.value(SUB_QUESTIONS).orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    public List<String> searchResults() {
        return (List<String>) this.value(SEARCH_RESULTS).orElse(List.of());
    }

    public String draft() {
        return this.<String>value(DRAFT).orElse("");
    }

    public String critique() {
        return this.<String>value(CRITIQUE).orElse("");
    }

    public int iteration() {
        return this.<Integer>value(ITERATION).orElse(0);
    }

    public Optional<String> finalReport() {
        return this.value(FINAL_REPORT);
    }

}
