(ns pet-care.store
  "SSoT for the ISCO-08 5164 independent pet-grooming-and-animal-care
  sole-proprietor actor. Store is a protocol injected into the
  `pet-care.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    animal   — a registered animal (:animal-id, :name)
    record   — a committed operating record under an animal
               (groom-support step, monitor entry, anxious/aggressive-
               animal handling, sedation procedure) — written ONLY via
               commit-record!, never mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (animal [s animal-id])
  (records-of [s animal-id])
  (ledger [s])
  (register-animal! [s animal])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (animal [_ animal-id] (get-in @a [:animals animal-id]))
  (records-of [_ animal-id] (filter #(= animal-id (:animal-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-animal! [s animal]
    (swap! a assoc-in [:animals (:animal-id animal)] animal) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:animals {} :records [] :ledger []} seed)))))
