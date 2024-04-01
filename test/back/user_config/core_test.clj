(ns user-config.core-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [user-config.core :refer [load-from-file]]
            [babashka.fs :as fs]
            [clojure.data.json :as json]))

;; fixtures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def fixture-base-path        (str (fs/absolutize "test/back/fixtures")))
(def user-config-file-path    (str (fs/path fixture-base-path "user-config.json")))
(def invalid-json-file-path   (str (fs/path fixture-base-path "not-valid-json-file.json")))
(def invalid-port-file-path   (str (fs/path fixture-base-path "invalid-port.json")))

(defn create-user-config-file []
  (spit user-config-file-path (json/write-str {:root-dir-path fixture-base-path
                                               :param1        "value1"
                                               :server-port   8882}))
  (spit invalid-json-file-path "I'm not JSON content")
  (spit invalid-port-file-path (json/write-str {:server-port   "8882"})))

(defn clean-user-config-file []
  (for [file-path [user-config-file-path
                   invalid-json-file-path
                   invalid-port-file-path]]
    (fs/delete file-path)))

(defn with-fs-tree [f]
  (create-user-config-file)
  (f)
  (clean-user-config-file))


(use-fixtures :once with-fs-tree)


;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest json-string->map-test
  (testing "converting a JSON string into a user config map"
    (is (= #:user-config{:server-port 45}
           (#'user-config.core/json-string->map "{\"server-port\" : 45}"))
        "converts to a namespaced keys map")))

(deftest load-from-file-test
  (testing "Load and validate configuration from a JSON file"
    (is (= #:user-config{:param1 "value1"
                         :server-port 8882
                         :root-dir-path fixture-base-path}
           (load-from-file user-config-file-path))
        "load from json file and namespace keywords")

    (is (thrown? Exception (load-from-file "file_not_found"))
        "error when file not found")

    (is (thrown? Exception (load-from-file invalid-json-file-path))
        "error when file not json")

    (is (thrown? Exception (load-from-file invalid-port-file-path))
        "error when config value is invalid (port)")))

(deftest validate-user-config-test
  (testing "user config validation"

    (is (thrown? Exception (#'user-config.core/validate #:user-config{:server-port ""}))
        "server port can't be a string")
    (is (= #:user-config{:server-port 8881}
           (#'user-config.core/validate #:user-config{:server-port 8881})))

    (is (thrown? Exception (#'user-config.core/validate #:user-config{:open-browser 1}))
        "open-browser can't be integer")
    (is (= #:user-config{:open-browser true}
           (#'user-config.core/validate #:user-config{:open-browser true})))

    (is (thrown? Exception (#'user-config.core/validate #:user-config{:browse-url "not_valid"}))
        "browse-url must be a valid URL")
    (is (= #:user-config{:browse-url "http://hostname:888/path"}
           (#'user-config.core/validate #:user-config{:browse-url "http://hostname:888/path"})))

    (is (thrown? Exception (#'user-config.core/validate #:user-config{:root-dir-path ""}))
        "root-dir-path can't be empty string")
    (is (thrown? Exception (#'user-config.core/validate #:user-config{:root-dir-path 12}))
        "root-dir-path can't be integer")
    (is (thrown? Exception (#'user-config.core/validate #:user-config{:root-dir-path "../"}))
        "root-dir-path can't be a relative path")
    (is (thrown? Exception (#'user-config.core/validate #:user-config{:root-dir-path user-config-file-path}))
        "root-dir-path can't be a file path")

    (is (= #:user-config{:root-dir-path fixture-base-path}
           (#'user-config.core/validate #:user-config{:root-dir-path fixture-base-path}))
        "root-dir-path must be absolute path of existing dir"))) 