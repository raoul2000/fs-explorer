(ns spec-config-test
  (:require [clojure.test :refer (deftest testing is)]
            [clojure.spec.alpha :as s]))

(deftest spec-test
  (testing "spec test"

    (is (s/valid? :coll/non-empty-string-list ["a"]))
    (is (s/valid? :coll/non-empty-string-list ["a" "b" "b"]))
    (is (not (s/valid? :coll/non-empty-string-list [])))
    (is (not (s/valid? :coll/non-empty-string-list ["a" "b" 1])))
    (is (not (s/valid? :coll/non-empty-string-list ["" ""])))

    (is (s/valid? :predicate/name :starts-With))
    (is (not (s/valid? :predicate/name "starts-with")))

    (is (s/valid? :predicate/arg "a"))
    (is (s/valid? :predicate/arg ["a" "b"]))

    (is (not (s/valid? :predicate/arg :key)))
    (is (not (s/valid? :predicate/arg [])))
    (is (not (s/valid? :predicate/arg ["e" ""])))
    (is (not (s/valid? :predicate/arg "")) "blank string not allowed")

    (is (s/valid? :selector/predicates  {:start-with "s"}))
    (is (s/valid? :selector/predicates  {:start-with ["s" "t"]}))
    (is (s/valid? :selector/predicates  {:start-with ["s" "t"] :ends-with "a"}))
    (is (s/valid? :selector/predicates  {:start-with ["s" "t"] :ends-with ["a" "b"]}))

    (is (not (s/valid? :selector/predicates {"f" {:starts-with "s"}})))
    (is (not (s/valid? :selector/predicates {:greater-then ""})))
    (is (not (s/valid? :selector/predicates {:greater-then 1})))

    (is (s/valid? :type/selector "s"))
    (is (s/valid? :type/selector  {:starts-with "e"}))

    (is (not (s/valid? :type/selector 42)))
    (is (not (s/valid? :type/selector [42])))

    (is (s/valid? :type/definition {:type/selector "s"}))
    (is (s/valid? :type/definition {:type/selector {:start-with ["s" "t"] :ends-with ["a" "b"]}}))


    (is (s/valid? :user-config/type {"a" {:type/selector "X"}
                                     "b" {:type/selector {:starts-With ["a" "b" "c"]}}}))

    (is (not (s/valid? :user-config/type {"" {:type/selector "Z"}}))  "blank string not a valid type name"))
  
;;                                     
  ) 

