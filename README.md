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

## License

AGPL-3.0-or-later.
