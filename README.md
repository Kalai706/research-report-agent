Research & Report Agent (LangGraph4j)

A hands-on agentic workflow: given a topic, the agent plans sub-questions, searches the web, drafts a report, 
critiques its own draft, and loops between critique <-> revise until the draft is approved (or hits a revision cap)
— then finalizes.

# Initial Architecture ( Refer : branch: [initial-architecture](https://github.com/Kalai706/research-report-agent/tree/feature/project-structure) )
```
START → plan → search → synthesize → critique ─┬─(approved)──→ finalize → END
                            ↑                  │
                            └──── revise ←──(needs work)
```
# Update with RAG ( Retrieval Augmented Generation ) ( Refer : branch: [Check-RAG-First](https://github.com/Kalai706/research-report-agent/tree/feature/check-rag-first) )

```
START → plan → ragSearch -> search → synthesize → critique ─┬─(approved)──→ finalize → END
                                ↑                  │
                                └──── revise ←──(needs work)
```

## Prerequisites: 
   1. Dependency : Vector DB - I am using PG vector ( for localset use docker, Pgadmin to view the table)
   2. Dependency : document reader - Tika, PDF reader - i am using Tika
   3. Dependency : starter-model-transformer

## Step by Step Explain of RAG - Document reader
    1. Read the document using Tika and extract the text content.
    2. Split the extracted text into smaller chunks for processing.
    3. starter-model-transformer used to generate chunk into vector embeddings.
    4. Store the embeddings in a vector database (PG vector) for efficient retrieval.

## Step by Step Explain of RAG in Research & Report Agent
    1. The agent receives a topic or query from the user.
    2. The agent plans sub-questions related to the topic.
    3. The agent ragSearch the vector DB for the relevant information
        based on the sub-questions and collected it as a source
    4. The agent searchnode searches the web for additional information related to the topic and unanswered questions.
    5. The agent synthesizes the information from the vector DB and web search to draft a report.
    6. The agent critiques its own draft, identifying areas for improvement.
    7. The agent loops between critique and revise until the draft is approved or hits a revision cap.
    8. Once the draft is approved, the agent finalizes the report and presents it to the user.

# Implement HITL  ( Refer : branch: [Check-RAG-First](https://github.com/Kalai706/research-report-agent/tree/feature/implement-HITL) )
``` 
START -> plan -> retrieve (RAG) --(covered)--> synthesize -> critique------------------------------ humanReview [PAUSE HERE]
                        |                                        |                                         /        \     
                    (gaps found)                          (self-approved /                              (approved)    (rejected)  
                        v                                   revision cap)                                  v              v
                     search 
                                                                                                         finalize        revise
                                                                                                             |              |
                                                                     
                                                                                                           END         critique (loop)
                                                                                                                   
```

 