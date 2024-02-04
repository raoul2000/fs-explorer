(ns system-test
  (:require [clojure.test :refer (deftest testing is)]
            [system :as sys]))


(deftest config-init-test
  (testing "initialisation of the config map"
    (is (= {:port nil :open-browser? nil :browse-url nil}
           (sys/init-app-config  {})))


    (is (= {:port 111 :open-browser? nil :browse-url nil}
           (sys/init-app-config  {:port 111})))

    (is (= {:port 222 :open-browser? nil :browse-url nil}
           (sys/init-app-config  {:port 111
                                  :user-config {:user-config/server-port 222}}))
        "user configured port value takes priority over default")

    (is (= {:port 222 :open-browser? true :browse-url nil}
           (sys/init-app-config  {:port 111
                                  :user-config {:user-config/server-port 222
                                                :user-config/open-browser true}}))
        "open-browser read from user config")

    (is (= {:port 222 :open-browser? false :browse-url nil}
           (sys/init-app-config  {:port 111
                                  :open-browser? true
                                  :user-config {:user-config/server-port 222
                                                :user-config/open-browser false}}))
        "open-browser from user config overwrite default")

    (is (= {:port 222 :open-browser? true :browse-url "http://myurl.com"}
           (sys/init-app-config  {:port 111
                                  :user-config {:user-config/server-port 222
                                                :user-config/open-browser true
                                                :user-config/browse-url "http://myurl.com"}}))
        "browse-url read from user config")

    (is (= {:port 222 :open-browser? false :browse-url "http://myurl.com"}
           (sys/init-app-config  {:port 111
                                  :open-browser? true
                                  :browse-url "http://default.com"
                                  :user-config {:user-config/server-port 222
                                                :user-config/open-browser false
                                                :user-config/browse-url "http://myurl.com"}}))
        "browse-url from user config overwrite default")
    )) 
