package com.research_report_agent.demo.controller;

import com.research_report_agent.demo.agent.ReporterAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/research")
public class ResearchAgentController {

    private final ReporterAgentService researchAgentService;

    public ResearchAgentController(ReporterAgentService researchAgentService){
        this.researchAgentService = researchAgentService;
    }


    @PostMapping("/generate-report")
    public void startResearch(@RequestParam("topic") String topic) throws Exception {
        // Placeholder for starting the research process
        log.info("Starting research process for topic: {}", topic);
        researchAgentService.generateReport(topic);
        log.info("Finished research process for topic: {}", topic);
    }
}
