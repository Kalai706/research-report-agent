package com.research_report_agent.demo.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppConfiguration {

    @Value("${app.provider:gemini}")
    private String provider;
}
