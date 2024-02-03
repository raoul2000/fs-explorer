(ns system-test
  (:require [clojure.test :refer (deftest testing is)]
            [system :as sys]))

(deftest config-init-test
  (testing "initialisation of the config map"
    (is (= {:port nil}
           (sys/init-app-config  {})))


    (is (= {:port 111}
           (sys/init-app-config  {:port 111})))

    (is (= {:port 222}
           (sys/init-app-config  {:port 111
                                  :user-config {:user-config/server-port 222}}))
        "user configured port value takes priority over default"))) 