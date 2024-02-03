(ns user-config-test
  (:require [clojure.test :refer (deftest testing is)]
            [user-config :refer (load-from-file)]))


(deftest load-from-file-test
  (testing "Load and validate configuration from a JSON file"
    (is (= #:user-config{:param1 "value1"
                         :server-port 8882}
           (load-from-file "test/back/fixture/config-ok.json")))

    (is (thrown? Exception (load-from-file "file_not_found"))
        "error when file not found")

    (is (thrown? Exception (load-from-file "test/back/fixture/config-ko.json"))
        "error when file not not json")

    (is (thrown? Exception (load-from-file "test/back/fixture/config-invalid-port.json"))
        "error when config value is invalid (port)"))) 