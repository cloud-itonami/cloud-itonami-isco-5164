(ns pet-care.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [pet-care.store :as store]
            [pet-care.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-animal! st {:animal-id "animal-1" :name "Biscuit"})
    st))

(deftest ok-on-clean-groom-support
  (let [st (fresh-store)
        proposal {:op :groom-support :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:animal-id "animal-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-animal
  (let [st (fresh-store)
        proposal {:op :groom-support :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:animal-id "no-such-animal"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-animal (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :groom-support :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:animal-id "animal-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-anxious-aggressive-animal-operation
  (let [st (fresh-store)
        proposal {:op :operate-near-anxious-aggressive-animal :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:animal-id "animal-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-sedation-procedure
  (let [st (fresh-store)
        proposal {:op :sedation-procedure :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:animal-id "animal-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :groom-support :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:animal-id "animal-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:animal-id "animal-1" :op :monitor})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "animal-1"))))
    (is (= 1 (count (store/ledger st))))))
