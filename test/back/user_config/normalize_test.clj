(ns user-config.normalize-test
  (:require [clojure.test :refer (deftest testing is)]
            [user-config.normalize :refer [normalize]]))


(deftest normalize-test
  (testing "Normalize a map into a user-config map"
    (is (= #:user-config{:server-port 222}
           (normalize {"server-port" 222}))
        "adds the user-config namespace"))) 