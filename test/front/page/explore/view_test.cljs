(ns page.explore.view-test
  (:require [page.explore.view :as view]
            [cljs.test :refer (deftest is testing)]))


(deftest dummy-test
  (is true))

(deftest merge-actions-map-test
  (is (= {:name "a2"}
         (view/find-type-action [{:name "a1"}
                                 {:name "a2"}
                                 {:name "a3"}] "a2"))
      "find action map")

  (is (= {:name "a2"
          :label "A2"}
         (view/find-type-action [{:name "a1"
                                  :label "A1"}
                                 {:name "a2"
                                  :label "A2"}
                                 {:name "a3"
                                  :label "A3"}] "a2"))
      "find more action map")
  (is (nil?
       (view/find-type-action [{:name "a1"
                                :label "A1"}
                               {:name "a2"
                                :label "A2"}
                               {:name "a3"
                                :label "A3"}] "a4"))
      "returns nil when not found"))

(deftest action-label-test
  (testing "resolve action label to display to user"
    (is (= "A1"
           (view/action-label {:name "A1"}
                              [{:name "A1"}]))
        "returns name when no label")

    (is (= "L1"
           (view/action-label {:name "A1"
                               :label "L1"}
                              [{:name "A1"}]))
        "returns label at type level when only one")

    (is (= "LA1"
           (view/action-label {:name "A1"}
                              [{:name "A1"
                                :label "LA1"}]))
        "returns label at actions definition level when only one")

    (is (= "L1"
           (view/action-label {:name "A1"
                               :label "L1"}
                              [{:name "A1"
                                :label "LA1"}]))
        "returns label at actions definition level when both"))) 