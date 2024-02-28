(ns user-config-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [user-config :refer (load-from-file)]
            [babashka.fs :as fs]
            [clojure.data.json :as json]))

;; fixtures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def fixture-base-path       (str (fs/absolutize "test/back/fixtures")))
(def user-config-file-path   (str (fs/path fixture-base-path "user-config.json")))


(defn create-user-config-file []
  (spit user-config-file-path (json/write-str {:root-dir-path fixture-base-path
                                               :param1        "value1"
                                               :server-port   8882})))

(defn clean-user-config-file []
  (fs/delete user-config-file-path))

(defn with-fs-tree [f]
  (create-user-config-file)
  (f)
  (clean-user-config-file))

(use-fixtures :once with-fs-tree)


;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(deftest load-from-file-test
  (testing "Load and validate configuration from a JSON file"
    (is (= #:user-config{:param1 "value1"
                         :server-port 8882
                         :root-dir-path fixture-base-path}
           (load-from-file user-config-file-path)))

    (is (thrown? Exception (load-from-file "file_not_found"))
        "error when file not found")

    (is (thrown? Exception (load-from-file "test/back/fixture/config-ko.json"))
        "error when file not not json")

    (is (thrown? Exception (load-from-file "test/back/fixture/config-invalid-port.json"))
        "error when config value is invalid (port)"))) 