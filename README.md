# cloud-itonami-isco-5164

Open Occupation Blueprint for **ISCO-08 5164**: Pet Groomers and Animal Care Workers.

This repository designs a forkable OSS business for an independent pet groomer/animal-care worker: a grooming-support robot performs supply setup and cleanup tasks under a governor-gated actor, so the practice keeps its own service and health records instead of renting a closed pet-care SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a grooming-support robot performs supply setup, coat brushing and cleanup tasks alongside a supervising groomer under an actor that proposes
actions and an independent **Pet Care Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
operating near an anxious/aggressive animal, or any procedure involving sedation) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
owner consent + animal health record + service scope
        |
        v
Pet Care Advisor -> Pet Care Governor -> groom-support/monitor, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `5164`). Required capabilities:

- :robotics
- :forms
- :telemetry
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Reference implementation (`:maturity :implemented`)

Full itonami Actor pattern (per ADR-2607011000 / CLAUDE.md's Actors
section, alongside `cloud-itonami-isco-6130`, `-8160`, `-2166`, `-2641`,
`-2651`, `-2652`, `-2654`, `-1219`, `-1223`, `-1330`, `-1341`, `-1349`,
`-1412`, `-1439`, `-2144`, `-2320`, `-2411`, `-2422`, `-2431`, `-2621`,
`-2634`, `-3122`, `-3123`, `-3141`, `-3255`, `-3339`, `-3512`, `-4120`,
`-4131`, `-4132`, `-4211`, `-4224`, `-4229`, `-4322`, `-4413`, `-4415`,
`-5120` and `-5162`): a real
[`kotoba-lang/langgraph`](https://github.com/kotoba-lang/langgraph)
`StateGraph`, with the Advisor and Governor as distinct graph nodes and
human-in-the-loop interrupt/resume via checkpointing.

```text
:intake -> :advise -> :govern -> :decide -+-> :commit            (:ok? true)
                                           +-> :request-approval   (:escalate? true, interrupt-before)
                                           +-> :hold               (:hard? true)
```

- `src/pet_care/store.cljc` — `Store` protocol +
  `MemStore`: registered animals, committed records, an append-only
  audit ledger.
- `src/pet_care/advisor.cljc` — `Advisor` protocol;
  `mock-advisor` (deterministic, default) proposes a groom-support or
  monitor operation from a request; `llm-advisor` wraps a
  `langchain.model/ChatModel` — either way the advisor only ever
  produces a `:propose`-effect proposal, never a committed record, and
  LLM parse failures always yield `confidence 0.0` (forces escalation,
  never fabricated confidence).
- `src/pet_care/governor.cljc` —
  `PetCareGovernor/check`: a pure function, wired as its own
  `:govern` node. Hard invariants (unregistered animal, a proposal
  whose `:effect` isn't `:propose`) always route to `:hold`. Escalation
  invariants (`:operate-near-anxious-aggressive-animal`,
  `:sedation-procedure`, or low advisor confidence) always route to
  `:request-approval` — an `interrupt-before` node that the graph
  checkpoints and only resumes on explicit human approval
  (`actor/approve!`), matching the README's robotics-premise statement
  that operating near an anxious/aggressive animal, or any procedure
  involving sedation, always requires human sign-off.
- `src/pet_care/actor.cljc` — `build-graph`, `run-request!`,
  `approve!`: the `langgraph.graph/state-graph` wiring itself.

```bash
clojure -M:test
```

This is what backs this repo's `:maturity :implemented` entry in
[`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation).

## License

AGPL-3.0-or-later.
