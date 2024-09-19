(ns config-test
  (:require [clojure.spec.alpha :as spec]
            [clojure.test :refer (deftest testing is are)]
            [config :as conf])
  (:import [clojure.lang ExceptionInfo]))

((deftest spec-test
   (testing "server-port validation"
     (are [result arg-map] (= result (spec/valid? :config/server-port (:port arg-map)))
       true      {:port 3000}
       false     {:port -1}
       false     {:port 65353}
       false     {:port 1.25}
       false     {:port "8888"}))

   (testing "browse url validation"
     (are [result arg-map] (= result (spec/valid? :config/browse-url (:url arg-map)))
       true      {:url "http://host:8888/path/to/index.html"}
       true      {:url "http://host"}
       true      {:url "https://host:8888/path/to/index.html"}
       false     {:url "http://host:XXX/path/to/index.html"}
       true      {:url "ftp://host:8888/path/to/index.html"}
       false     {:url "hp://host:8888/path/to/index.html"}))

   (testing "selectors"
     (is (not (spec/valid? :config.type/selectors {:starts-with "str"
                                                   :ends-with  "txt"}))
         "selectors is not a map anymore")

     (is (spec/valid? :config.type/selectors [{:starts-with "str"}
                                              {:ends-with  "txt"}])
         "selectors is a collection of maps")

     (is (not (spec/valid? :config.type/selectors []))
         "selectors can't be empty"))

   (testing "type definition"
     (is (not (spec/valid? :config.type/definition #:config.type{}))
         "empty type definition is not allowed")

     (is (spec/valid? :config.type/definition #:config.type{:name "type name"})
         "type name is required")

     (is (not (spec/valid? :config.type/definition #:config.type{:name ""}))
         "type name can't be blank")

     (is (spec/valid? :config.type/definition #:config.type{:name "type name"
                                                            :selector [{:starts-with "str"}
                                                                       {:ends-with  "txt"}]})
         "selectors when configured must not be empty"))

   (testing "types config"
     (is (not (spec/valid? :config/types []))
         "empty type config is not allowed")

     (is (spec/valid? :config/types [#:config.type{:name "type name"}
                                     #:config.type{:name "other type name"}])
         "type config maps must be namespaced"))

   (testing "actions config"
     (is (not (spec/valid? :config/actions []))
         "empty actions config is not allowed")

     (is (spec/valid? :config/actions [#:config.action{:name "action1"
                                                       :exec "prog.exe"}
                                       #:config.action{:name "action2"
                                                       :exec "prog.exe"}])
         "actions are valid")

     (is (not (spec/valid? :config/actions [#:config.action{:name "action1"}]))
         "key :exec is required")

     (is (not (spec/valid? :config/actions [#:config.action{:name "action1"
                                                            :exec "prog.exe"
                                                            :args "scalar"}]))
         "key :arg must be a seq"))))

(deftest add-ns-to-user-config-test
  (testing "adding namespace to user config map"
    (is (= "value" (:config/key (conf/add-ns-to-user-config {:key "value"})))
        "namespace 'config' is added to top key")

    (is (every? #(= "config" (namespace (first %)))
                (conf/add-ns-to-user-config {:key1 "value1"
                                             :key2 "value2"}))
        "namespace 'config' is added to all top level keys")

    (is (= #:config{:types
                    '(#:config.type{:name "type1", :selectors [{:equals "value"}]}
                      #:config.type{:name "type2", :selectors [{:ends-with "value"}]})}
           (conf/add-ns-to-user-config {:types [{:name      "type1"
                                                 :selectors [{:equals "value"}]}
                                                {:name      "type2"
                                                 :selectors [{:ends-with "value"}]}]}))
        "adds namespace 'config.type' to all type definition maps")))

(deftest merge-config-test
  (testing "mergining 2 configs"
    (is (= #:config{:root-dir-path "path user"}
           (conf/merge-configs #:config{:root-dir-path "path default"}
                               #:config{:root-dir-path "path user"}))
        "user param replace default")

    (is (= #:config{:root-dir-path "path default",
                    :open-browser false}
           (conf/merge-configs #:config{:root-dir-path "path default"}
                               #:config{:open-browser false}))
        "when not re-defined, default param remains")

    (is (= #:config{:server-port 9999,
                    :browse-url "http://localhost:9999/"}
           (conf/merge-configs #:config{:server-port 8888}
                               #:config{:server-port 9999}))
        "browser url is updated with user configured server port value")

    (is (= #:config{:server-port 7777,
                    :browse-url "http://127.0.0.1:9999"}
           (conf/merge-configs #:config{:server-port 8888}
                               #:config{:server-port 7777
                                        :browse-url "http://127.0.0.1:9999"}))
        "user param browser url is not modified even when server port is modified")))


(deftest create-config-test
  (testing "create app config"
    (is (= conf/default-config (conf/create-config nil))
        "when no file path is given, app config is default config")

    (is (= #:config{:server-port   7777,
                    :root-dir-path "c:\\tmp",
                    :open-browser  true,
                    :browse-url    "http://localhost:7777/",
                    :types         '(#:config.type{:name "MY_FIRST_TYPE",
                                                   :selectors ({:match-regexp ".*/README.md$"}
                                                               {:ends-with "md"})}
                                     #:config.type{:name "MY_SECOND_TYPE",
                                                   :selectors ({:ends-with-ignore-case "txt"})})}

           (conf/create-config "./test/back/fixtures/config_test-1.yaml"))
        "when file path is given, merge with default config")

    (is (thrown-with-msg? ExceptionInfo  #"Configuration file not found"
                          (conf/create-config "./not_found"))
        "throws when file not found")

    (is (thrown-with-msg? ExceptionInfo  #"Invalid User Configuration"
                          (conf/create-config "./test/back/fixtures/config_test-2.yaml"))
        "throws when invalid user configuration")
    (try
      (conf/create-config "./test/back/fixtures/config_test-2.yaml")
      (catch ExceptionInfo ex
        (let [error-info (ex-data ex)]
          (is (= "./test/back/fixtures/config_test-2.yaml" (:file error-info))
              "error info includes filename")
          (is  (:error error-info)
               "error info includes reason")))))) 

