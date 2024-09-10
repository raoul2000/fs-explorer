(ns server.domain.explorer.type-test
  (:require [clojure.test :refer (deftest testing is  are)]
            [domain.explorer.type :as t]))


(deftest equals-selector-test
  (let [equals-pred (:equals t/file-selectors-catalog)]
    (testing "the :equals selector"
      (is (equals-pred "val" nil {:name "val"})
          "test :name key value equals arg")

      (is (not (equals-pred "val" nil {:k "val"}))
          "returns false when map has no :name key"))))


(deftest create-selector-pred-test
  (testing "create a selector predicate"
    (is (fn?
         (t/create-selector-pred {:equals "val"}))
        "returns a function when map key equals selector key")

    (is (fn?
         (t/create-selector-pred {:starts-with "str"
                                  :equals "val"}))
        "returns a function when more than one map key equals selector key")

    (is (fn?
         (t/create-selector-pred {:k1 1
                                  :k2 2
                                  :equals "val"}))
        "returns a function when map include selector key")

    (is (nil?
         (t/create-selector-pred {:not_found "val"}))
        "returns nil when map key is not a selector key")

    (is (nil?
         (t/create-selector-pred {:not_found "val"
                                  :k1 1
                                  :k2 2}))
        "returns nil when no key matches a selector key")))


(deftest selector-match-test
  (testing "When a selector matches a file"
    (is (t/selector-match {:name "val"} {:equals "val"})
        "file should match (1)")

    (is (t/selector-match {:name "val"} {:option 1
                                         :equals "val"})
        "file should match (2)")

    (is (not (t/selector-match {:name "val"} {:equals "something"}))
        "file not should match")

    (is (not (t/selector-match {:name "val"} nil))
        "does not match when selector is nil")

    (is (not (t/selector-match {:name "val"} {}))
        "does not match when selector is empty map"))) 