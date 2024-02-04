(ns server.handler.greet-test
  (:require [clojure.test :refer (deftest is testing)]
            [server.handler.greet :as greet]))

(def greet-handler-polite (greet/create {:polite? true}))
(def greet-handler-default (greet/create {}))

(deftest greet-handler-test
  (testing "the /gret handler"

    (is (= 200
           (:status (greet-handler-polite {:params {:name "joe"}})))
        "respond with success status")

    (is (= 500
           (:status (greet-handler-polite {:params {:name "bob"}})))
        "responds with error status"))) 