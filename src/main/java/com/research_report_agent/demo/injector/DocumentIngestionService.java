package com.research_report_agent.demo.injector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;


    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void inject(Resource resource) {
        log.debug("Injecting document from resource: {}", resource.getFilename());
        try {
            // Read the document from the resource
            DocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> document = documentReader.get();

            // Split the document into chunks
            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(800)
                    .withMinChunkSizeChars(400)
                    .withKeepSeparator(true)
                    .withMaxNumChunks(1000)
                    .withMinChunkLengthToEmbed(5)
                    .build();
            List<Document> chunks = splitter.split(document);

            // Store the chunks in the vector store
            vectorStore.add(chunks);
            log.debug("Successfully injected document from resource: {}", resource.getFilename());
        } catch (Exception e) {
            log.error("Error during document ingestion: ", e);
        }

    }
}
