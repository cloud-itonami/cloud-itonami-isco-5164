(ns pet-care.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [pet-care.actor :as actor]
            [pet-care.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-animal! st {:animal-id "animal-1" :name "Biscuit"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:animal-id "animal-1" :op :groom-support :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "animal-1"))))))

(deftest holds-on-unregistered-animal-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:animal-id "no-such-animal" :op :groom-support :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-animal")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; sedation procedure always escalates (governor invariant)
        request {:animal-id "animal-1" :op :sedation-procedure :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "animal-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "animal-1")))))))
