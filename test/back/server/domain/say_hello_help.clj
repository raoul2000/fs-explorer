(ns server.domain.say-hello-help
  (:require [clojure.test :refer (deftest testing is)]
            [domain.say-hello :refer (say-hello)]))

(deftest say-hello-test
  (testing "when saying hello"
    (is (= "Good morning bob "
           (say-hello "bob" true))))) 