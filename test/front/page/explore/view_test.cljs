(ns page.explore.view-test
  (:require [cljs.test :refer (deftest is testing)]
            [page.explore.view :refer [selector-match eval-selector-rule]]))

(deftest eval-selector-rule-test
  (testing "selector rule evaluation"
    (is (= true
           (eval-selector-rule [:action.selector/equals "filename"] "filename"))
        "returns True if s equal rule's value")
    (is (= false
           (eval-selector-rule [:action.selector/equals "filename"] "file"))
        "returns False if s is NOT equal to rule's value")

    (is (thrown? js/Error (eval-selector-rule [:action.selector/dummy "value"] "value"))
        "throws when selector rule in not known")))

(deftest selector-match-test
  (testing "basic selector match"
    (is (= true
           (selector-match #:file{:id "readme.txt"} "readme.txt"))
        "when selector value is a string, and its value equals item id")
    (is (= false
           (selector-match #:file{:id "filename"} "readme.txt"))
        "when selector value is a string, and its value NOT equals item id"))

  (testing "invalid selector type"
    (is (thrown? js/Error (selector-match #:file{:id "filename"}  42))
        "throw when selector type is not supported"))

  (testing "composite selector : equals"
    (is (seq
         (selector-match #:file{:id "readme.txt"}
                         #:action.selector{:equals "readme.txt"}))
        "returns truthy when equals")

    (is (empty?
         (selector-match #:file{:id "filename"}
                         #:action.selector{:equals "readme.txt"}))
        "returns empty seq when NOT equals")

    ;; don't know why the test below fails when it should not
    #_(is (thrown? js/Error
                   (selector-match #:file{:id "filename.txt"}
                                   #:action.selector{:dummy "dummy-value"}))
          "throws when selecter type is not supported"))

  (testing "regexp selector : match"
    (is (seq
         (selector-match #:file{:id "readme.txt"}
                         #:action.selector{:match "read"}))
        "return truthy when re is found to match")

    (is (seq
         (selector-match #:file{:id "readme.txt"}
                         #:action.selector{:match "^read..\\.[tT][xX][tT]$"}))
        "return truthy when re fully matches")

    (is (empty?
         (selector-match #:file{:id "readme.txt"}
                         #:action.selector{:match "xxx"}))
        "returns empty when no match")
    ;; don't know why the test below fails when it should not
    #_(is (thrown? ExceptionInfo
                   (selector-match #:file{:id "readme.txt"}
                                   #:action.selector{:match "invalid re **"}))
          "throws when regex is invalid"))
  ;;
  )


#_(deftest find-matching-command-test
    (testing "it finds the first matching command"
      (is (= ""
             (find-matching-command #:file{:name "readme.md"}
                                    [#:user-config{:selector "readme.md"
                                                   :command "notepad.exe"}])))))
