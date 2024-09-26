(ns page.explore.view-test
  (:require [cljs.test :refer (deftest is testing)]
            ))


(deftest dummy-test
  (is true)
  )

#_(deftest find-matching-command-test
    (testing "it finds the first matching command"
      (is (= ""
             (find-matching-command #:file{:name "readme.md"}
                                    [#:user-config{:selector "readme.md"
                                                   :command "notepad.exe"}])))))
