(ns pet-care.governor
  "PetCareGovernor — the independent safety/traceability layer for
  the ISCO-08 5164 independent pet-grooming-and-animal-care actor.
  Wired as its own `:govern` node in `pet-care.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of animal
  provenance or aggressive-animal/sedation risk, so this MUST be a
  separate system able to reject a proposal (itonami actor pattern,
  per ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. animal provenance   — the request's animal must be registered.
    2. no-actuation        — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: operating near an anxious/aggressive animal,
  or any procedure involving sedation, always requires human sign-off):
    3. :op :operate-near-anxious-aggressive-animal.
    4. :op :sedation-procedure.
    5. low confidence (< `confidence-floor`)."
  (:require [pet-care.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:operate-near-anxious-aggressive-animal :sedation-procedure})

(defn- hard-violations [{:keys [proposal]} animal-record]
  (cond-> []
    (nil? animal-record)
    (conj {:rule :no-animal :detail "未登録 animal"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `pet-care.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [animal-record (store/animal store (:animal-id request))
        hard (hard-violations {:proposal proposal} animal-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
