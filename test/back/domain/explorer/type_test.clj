(ns domain.explorer.type-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [domain.explorer.type :as t]
            [babashka.fs :as fs])
  (:import [clojure.lang ExceptionInfo]))

;; fixtures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def fixture-base-path  (fs/absolutize "test/fixture/fs"))
(defn make-dirs [dirs]
  (doseq [dir dirs]
    (fs/create-dirs (fs/path fixture-base-path dir))))

(defn make-files [files]
  (doseq [file files]
    (fs/create-file (fs/path fixture-base-path file))))

(defn create-fs []
  (make-dirs ["dir1/dir2-1"
              "dir1/dir2-2"
              "dir1/dir2-3"
              "dir2/dir2-1"])
  (make-files ["dir1/file.txt"]))

(defn clean-fs []
  (fs/delete-tree fixture-base-path))

(defn with-fs-tree [f]
  (create-fs)
  (f)
  (clean-fs))

(use-fixtures :once with-fs-tree)

(deftest property-option-test
  (let [pred (:selector/starts-with t/file-selectors-catalog)]
    (testing "apply selector on the configured file item property"
      (is (pred "val" nil #:file{:name "val"})
          "default property is 'name'")

      (is (pred "val" #:selector{:property "path"} #:file{:name "val-1"
                                                          :path "val"})
          "property can be configured to be 'path'")

      (is (nil? (pred "val" #:selector{:property "not_found"} #:file{:name "val-1"
                                                                     :path "val"}))
          "return nil when the property name is not found")

      (is (nil? (pred "val" #:selector{:property "path"} #:file{:name "val-1"}))
          "returns nil when the file-item does not contain the configured property"))))

;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest matches-regexp-test
  (let [matches-regexp-pred (:selector/matches-regexp t/file-selectors-catalog)]
    (testing "the :matches-regexp selector"
      (is (matches-regexp-pred ".*" nil #:file{:name "file.txt"})
          "matches a single regexp")

      (is (not (matches-regexp-pred "\\d+" nil #:file{:name "file.txt"}))
          "does not matches a single regexp")

      (is (matches-regexp-pred ["\\d+" ".*"] nil #:file{:name "file.txt"})
          "matches one among several regexp")

      (is (not (matches-regexp-pred ["\\d+" "\\w*"] nil #:file{:name "file.txt"}))
          "does not match when all regexp don't match")

      (is (thrown-with-msg? ExceptionInfo  #"invalid Regular Exception syntax"
                            (matches-regexp-pred "*" nil #:file{:name "file.txt"}))
          "throw when the regexp is invalid")

      (is (thrown-with-msg? ExceptionInfo  #"invalid Regular Exception syntax"
                            (matches-regexp-pred ["\\d*" "*"] nil #:file{:name "file.txt"}))
          "throw when one regexp is invalid")))
  ;;
  )

(deftest is-directory-test
  (let [is-directory-pred (:selector/is-directory t/file-selectors-catalog)]
    (testing "the :is-directory selector"

      (is (not
           (is-directory-pred true nil #:file{:path (str (fs/path fixture-base-path "dir1/file.txt"))}))
          "dir1/file.txt is not a dir, selector fails")

      (is  (is-directory-pred false nil #:file{:path (str (fs/path fixture-base-path "dir1/file.txt"))})
           "dir1/file.txt is not a dir, selector matches")

      (is (not
           (is-directory-pred false nil #:file{:path (str (fs/path fixture-base-path "dir1"))}))
          "dir1 is a dir, selector fails")

      (is (is-directory-pred true nil #:file{:path (str (fs/path fixture-base-path "dir1"))})
          "dir1 is a dir, selector matches"))))


(deftest equals-selector-test
  (let [equals-pred (:selector/equals t/file-selectors-catalog)]
    (testing "the :equals selector"
      (is (equals-pred "val" nil #:file{:name "val"})
          "test :name key value equals arg")

      (is (not (equals-pred "val" nil #:file{:k "val"}))
          "returns false when map has no :name key")

      (is (not (equals-pred "val" nil #:file{:name "VAL"}))
          "is case sensitive")

      (is (equals-pred ["ab" "val" "cd"] nil #:file{:name "val"})
          "accept a list ofr string and returns true if one match is found")

      (is (not (equals-pred ["ab" "bc" "cd"] nil #:file{:name "val"}))
          "returns false if not match is found")

      (is (not (equals-pred [] nil #:file{:name "val"}))
          "returns false if no val is provided"))))

(deftest starts-with-selector-test
  (let [starts-with-pred (:selector/starts-with t/file-selectors-catalog)]
    (testing "the :starts-with selector"
      (is (starts-with-pred "val" nil #:file{:name "value"})
          "true when :name value starts with arg")

      (is (starts-with-pred "val" nil #:file{:name "val"})
          "true when :name value equals arg")

      (is (not (starts-with-pred "val" nil #:file{:name "some value"}))
          "false  when :name value does not starts with arg")

      (is (not (starts-with-pred "val" nil #:file{:name "VAL"}))
          "is case sensitive")

      (is (starts-with-pred ["ab" "val" "cd"] nil #:file{:name "val filename"})
          "accept a list ofr string and returns true if one match is found")

      (is (not (starts-with-pred ["ab" "bc" "cd"] nil #:file{:name "val"}))
          "returns false if not match is found")

      (is (not (starts-with-pred [] nil #:file{:name "val"}))
          "returns false if no val is provided"))))

(deftest ends-with-selector-test
  (let [ends-with-pred (:selector/ends-with t/file-selectors-catalog)]
    (testing "the :ends-with selector"

      (is (ends-with-pred "val" nil #:file{:name "the val"})
          "true when :name value ends with arg")

      (is (ends-with-pred "val" nil #:file{:name "val"})
          "true when :name value equals arg")

      (is (not (ends-with-pred "val" nil #:file{:name "some value"}))
          "false  when :name value does not end with arg")

      (is (not (ends-with-pred "val" nil #:file{:name "the VAL"}))
          "is case sensitive")

      (is (ends-with-pred ["ab" "val" "cd"] nil #:file{:name "filename val"})
          "accept a list ofr string and returns true if one match is found")

      (is (not (ends-with-pred ["ab" "bc" "cd"] nil #:file{:name "val"}))
          "returns false if not match is found")

      (is (not (ends-with-pred [] nil #:file{:name "val"}))
          "returns false if no val is provided"))))

(deftest create-selector-pred-test
  (testing "create a selector predicate"
    (is (fn?
         (t/create-selector-pred #:selector{:equals "val"}))
        "returns a function when map key equals selector key")

    (is (fn?
         (t/create-selector-pred #:selector{:starts-with "str"
                                            :equals "val"}))
        "returns a function when more than one map key equals selector key")

    (is (fn?
         (t/create-selector-pred #:selector{:k1 1
                                            :k2 2
                                            :equals "val"}))
        "returns a function when map include selector key")

    (is (nil?
         (t/create-selector-pred #:selector{:not_found "val"}))
        "returns nil when map key is not a selector key")

    (is (nil?
         (t/create-selector-pred #:selector{:not_found "val"
                                            :k1 1
                                            :k2 2}))
        "returns nil when no key matches a selector key")))

(deftest selector-match-test
  (testing "When a selector matches a file"
    (is (t/selector-match #:file{:name "val"} #:selector{:equals "val"})
        "file should match (1)")

    (is (t/selector-match #:file{:name "val"} #:selector{:option 1
                                                         :equals "val"})
        "file should match (2)")

    (is (not (t/selector-match #:file{:name "val"} #:selector{:equals "something"}))
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
                                    :selectors [#:selector{:equals "val"}]}
                      #:file{:name "val"})))

  (testing "match when two type definition is provided"
    (is (t/type-match #:config.type{:name      "type1"
                                    :selectors [#:selector{:equals      "val"}
                                                #:selector{:starts-with "va"}]}
                      #:file{:name "val"})))

  (testing "does not match when not all selectors match"
    (is (not (t/type-match #:type{:name      "type1"
                                  :selectors [#:selector{:equals      "val"}
                                              #:selector{:starts-with "other"}]}
                           #:file{:name "val"})))))

(deftest select-type-test
  (testing "select a type for a given file"
    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type1"
                                                                    :selectors [#:selector{:equals "file.txt"}]}])))
        "returns the matching type map")

    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type1"
                                                                    :selectors [#:selector{:starts-with "file"}
                                                                                #:selector{:ends-with   "txt"}]}])))
        "returns the matching type map when all selectors match")

    (is (= "type1" (:type/name (t/select-type #:file{:name "file.txt"}
                                              [#:type{:name      "type2"
                                                      :selectors [#:selector{:starts-with "file"}
                                                                  #:selector{:ends-with   "md"}]}
                                               #:type{:name      "type1"
                                                      :selectors [#:selector{:starts-with "file"}
                                                                  #:selector{:ends-with   "txt"}]}])))
        "ignore type with no matching selectors")

    (is (= "type1" (:config.type/name (t/select-type #:file{:name "file.txt"}
                                                     [#:config.type{:name      "type1"
                                                                    :selectors [#:selector{:starts-with "file"}
                                                                                #:selector{:ends-with   "txt"}]}
                                                      #:config.type{:name      "type2"
                                                                    :selectors [#:selector{:equals "file.txt"}]}])))
        "selects first type that match in seq order"))

  (testing "when no type definition is provided returns nil"
    (is (nil? (t/select-type {:config.type/name "file.txt"} nil)))
    (is (nil? (t/select-type {:config.type/name "file.txt"} [])))))



(deftest ignored-type-test
  (testing "predicate for ignored type"
    (is (= nil
         (t/ignored-type [#:type{:name      "type1"}
                          #:type{:name      "type2"}]
                         #:file{:type     "type1"}))
        "returns falsy (nil) when file-m type is not ignored")

    (is (= true
           (t/ignored-type [#:type{:name      "type1"}
                            #:type{:name      "type2"
                                   :ignore   true}]
                           #:file{:type "type2"}))
        "returns true when file-m type is ignored"))) 

(deftest infer-type-test
  (testing "Infer type for file map"
    (is (= {:file/name "file_to_test.txt"
            :file/type "type1"}
           (t/infer-type [#:type{:name      "type1"
                                 :selectors [#:selector{:starts-with "file"}
                                             #:selector{:ends-with   "txt"}]}
                          #:type{:name      "type2"
                                 :selectors [#:selector{:equals "file.txt"}]}]
                         #:file{:name "file_to_test.txt"}))
        "adds key :file/type with the inferred type as value")

    (is (= {:file/name "no_match.txt"}
           (t/infer-type [#:type{:name      "type1"
                                 :selectors [#:selector{:starts-with "file"}
                                             #:selector{:ends-with   "txt"}]}
                          #:type{:name      "type2"
                                 :selectors [#:selector{:equals "file.txt"}]}]
                         #:file{:name "no_match.txt"}))
        "does not modify the file map")))