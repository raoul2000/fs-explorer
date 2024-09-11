(ns domain.say-hello-test
  (:require [clojure.test :refer (deftest testing is)]
            [domain.say-hello :refer (say-hello)]))

(deftest say-hello-test
  (testing "when saying hello"
    (is (= "Good morning joe !"
           (say-hello "joe" true)))

    (is (= "Hi joe !"
           (say-hello "joe" false))
        "saying hello not polite")

    (is (thrown? Exception (say-hello "bob" false))
        "error for 'bob' not polite")

    (is (thrown? Exception (say-hello "bob" true))
        "error for 'bob' being polite"))) 