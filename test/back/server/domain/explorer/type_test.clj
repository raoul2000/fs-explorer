(ns domain.explorer.type-test
  (:require [clojure.test :refer (deftest testing is)]
            [domain.explorer.type :as t]))


(deftest equals-selector-test
  (let [equals-pred (:equals t/file-selectors-catalog)]
    (testing "the :equals selector"
      (is (equals-pred "val" nil #:file{:name "val"})
          "test :name key value equals arg")

      (is (not (equals-pred "val" nil #:file{:k "val"}))
          "returns false when map has no :name key")

      (is (not (equals-pred "val" nil #:file{:name "VAL"}))
          "is case sensitive"))))

(deftest starts-with-selector-test
  (let [starts-with-pred (:starts-with t/file-selectors-catalog)]
    (testing "the :starts-with selector"
      (is (starts-with-pred "val" nil #:file{:name "value"})
          "true when :name value starts with arg")

      (is (starts-with-pred "val" nil #:file{:name "val"})
          "true when :name value equals arg")

      (is (not (starts-with-pred "val" nil #:file{:name "some value"}))
          "false  when :name value does not starts with arg")

      (is (not (starts-with-pred "val" nil #:file{:name "VAL"}))
          "is case sensitive"))))


(deftest ends-with-selector-test
  (let [ends-with-pred (:ends-with t/file-selectors-catalog)]
    (testing "the :ends-with selector"

      (is (ends-with-pred "val" nil #:file{:name "the val"})
          "true when :name value ends with arg")

      (is (ends-with-pred "val" nil #:file{:name "val"})
          "true when :name value equals arg")

      (is (not (ends-with-pred "val" nil #:file{:name "some value"}))
          "false  when :name value does not end with arg")

      (is (not (ends-with-pred "val" nil #:file{:name "the VAL"}))
          "is case sensitive"))))



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
    (is (t/selector-match #:file{:name "val"} {:equals "val"})
        "file should match (1)")

    (is (t/selector-match #:file{:name "val"} {:option 1
                                               :equals "val"})
        "file should match (2)")

    (is (not (t/selector-match #:file{:name "val"} {:equals "something"}))
        "file not should match")

    (is (not (t/selector-match #:file{:name "val"} nil))
        "does not match when selector is nil")

    (is (not (t/selector-match #:file{:name "val"} {}))
        "does not match when selector is empty map")))

(deftest type-match-test
  (testing "match when no type definition is provided"
    (is (t/type-match nil #:file{:name "val"}))
    (is (t/type-match []  #:file{:name "val"}))
    (is (t/type-match {}  #:file{:name "val"})))

  (testing "match when one type definition is provided"
    (is (t/type-match #:config.type{:name      "type1"
                                    :selectors [{:equals "val"}]}
                      #:file{:name "val"})))

  (testing "match when two type definition is provided"
    (is (t/type-match #:config.type{:name      "type1"
                                    :selectors [{:equals      "val"}
                                                {:starts-with "va"}]}
                      #:file{:name "val"})))

  (testing "does not match when not all selectors match"
    (is (not (t/type-match #:config.type{:name      "type1"
                                         :selectors [{:equals      "val"}
                                                     {:starts-with "other"}]}
                           #:file{:name "val"})))))

(deftest select-type-test
  (testing "select a type for a given file"
    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type1"
                                                                    :selectors [{:equals "file.txt"}]}])))
        "returns the matching type map")

    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type1"
                                                                    :selectors [{:starts-with "file"}
                                                                                {:ends-with   "txt"}]}])))
        "returns the matching type map when all selectors match")

    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type2"
                                                                    :selectors [{:starts-with "file"}
                                                                                {:ends-with   "md"}]}
                                                      #:config.type{:name      "type1"
                                                                    :selectors [{:starts-with "file"}
                                                                                {:ends-with   "txt"}]}])))
        "ignore type with no matching selectors")

    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type1"
                                                                    :selectors [{:starts-with "file"}
                                                                                {:ends-with   "txt"}]}
                                                      #:config.type{:name      "type2"
                                                                    :selectors [{:equals "file.txt"}]}])))
        "selects first type that match in seq order"))

  (testing "when no type definition is provided returns nil"
    (is (nil? (t/select-type {:config.type/name "file.txt"} nil)))
    (is (nil? (t/select-type {:config.type/name "file.txt"} [])))))

(deftest infer-type-test
  (testing "Infer type for file map"
    (is (= {:file/name "file_to_test.txt"
            :file/type "type1"}
           (t/infer-type [#:config.type{:name      "type1"
                                        :selectors [{:starts-with "file"}
                                                    {:ends-with   "txt"}]}
                          #:config.type{:name      "type2"
                                        :selectors [{:equals "file.txt"}]}]
                         #:file{:name "file_to_test.txt"}))
        "adds key :file/type with the inferred type as value")

    (is (= {:file/name "no_match.txt"}
           (t/infer-type [#:config.type{:name      "type1"
                                        :selectors [{:starts-with "file"}
                                                    {:ends-with   "txt"}]}
                          #:config.type{:name      "type2"
                                        :selectors [{:equals "file.txt"}]}]
                         #:file{:name "no_match.txt"}))
        "does not modify the file map"))) 
