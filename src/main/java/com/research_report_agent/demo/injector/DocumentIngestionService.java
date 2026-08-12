package com.research_report_agent.demo.injector;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
             //1. Recursive chunking Strategy : walk through Paragraph, Sentence, and Word to create chunks of text that are within the specified size limit.
            DocumentSplitter docRecSplitter = DocumentSplitters.recursive(800,200);
            List<TextSegment> segments = docRecSplitter.splitAll((dev.langchain4j.data.document.Document) document);
            Map<String,Object> custom_metadata = Map.of("year", "2026");
            List<Document> chunks = segments.stream()
                    .map(segment -> new Document(segment.text(),
                            segment.metadata() != null ? addMetaData(custom_metadata,segment.metadata().toMap()) : custom_metadata))
                    .toList();


//            //1. Fixed chunking Strategy : hard split of chunk
//            TokenTextSplitter splitter = TokenTextSplitter.builder()
//                    .withChunkSize(800)
//                    .withMinChunkSizeChars(400)
//                    .withKeepSeparator(true)
//                    .withMaxNumChunks(1000)
//                    .withMinChunkLengthToEmbed(5)
//                    .build();
//            List<Document> chunks = splitter.split(document);

            // Store the chunks in the vector store
            vectorStore.add(chunks);
            log.debug("Successfully injected document from resource: {}", resource.getFilename());
        } catch (Exception e) {
            log.error("Error during document ingestion: ", e);
        }

    }

    private Map<String,Object> addMetaData(Map<String,Object> map1,Map<String,Object> map2){
        return Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
