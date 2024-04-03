(ns user-config.normalize-test
  (:require [clojure.test :refer (deftest testing is)]
            [user-config.normalize :refer [normalize]]))


(deftest normalize-test
  (testing "Normalize a map into a user-config map"
    (is (= {}
           (normalize {}))
        "empty map is a valid user-config")

    (is (= #:user-config{:server-port   222
                         :open-brwoser  true
                         :root-dir-path "/folder/folder"}
           (normalize {"server-port"    222
                       "open-brwoser"   true
                       "root-dir-path"  "/folder/folder"}))
        "adds the user-config namespace"))) 