Research & Report Agent (LangGraph4j)

A hands-on agentic workflow: given a topic, the agent plans sub-questions, searches the web, drafts a report, 
critiques its own draft, and loops between critique <-> revise until the draft is approved (or hits a revision cap)
— then finalizes.

# Architecture:
```
START → plan → search → synthesize → critique ─┬─(approved)──→ finalize → END
                            ↑                  │
                            └──── revise ←──(needs work)
```