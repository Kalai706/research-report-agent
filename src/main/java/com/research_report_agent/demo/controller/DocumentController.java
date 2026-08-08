package com.research_report_agent.demo.controller;

import com.research_report_agent.demo.injector.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestParam("file") MultipartFile file){
        documentIngestionService.inject(file.getResource());
        return ResponseEntity.accepted().body("Document ingestion started for file: " + file.getOriginalFilename());
    }

}
