(ns config-test
  (:require [clojure.spec.alpha :as spec]
            [clojure.test :refer (deftest testing is are)]
            [config :as conf])
  (:import [clojure.lang ExceptionInfo]))

(deftest spec-test
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
    (is (not (spec/valid? :type/selectors {:starts-with "str"
                                           :ends-with  "txt"}))
        "selectors is not a map anymore")

    (is (spec/valid? :type/selectors [#:selector{:starts-with "str"}
                                      #:selector{:ends-with  "txt"}])
        "selectors is a collection of maps")

    (is (not (spec/valid? :type/selectors []))
        "selectors can't be empty"))

  (testing "type definition"
    (is (not (spec/valid? :config/types #:config.type{}))
        "empty type definition is not allowed")

    (is (not (spec/valid? :type/def  #:type{:name      "type name"}))
        "selector is required")

    (is (spec/valid? :type/def  #:type{:name      "type name"
                                       :selectors [#:selector{:starts-with "abc"}]})
        "type name and selector ar required")

    (is (not (spec/valid? :type/def #:type{:name ""
                                           :selector [#:selector{:starts-with "abc"}]}))
        "type name can't be blank")

    (is (not (spec/valid? :type/def #:type{:name "type name"
                                           :selector []}))
        "selectors when configured must not be empty"))

  (testing "types action"
    (is (not (spec/valid? :type/actions []))
        "empty type actions is not allowed")

    (is (spec/valid? :type/actions [#:action{:name "action1"
                                             :exec "prog.exe"}])))

  (testing "types config"
    (is (not (spec/valid? :config/types []))
        "empty type config is not allowed")

    (is (spec/valid? :config/types [#:type{:name "type name"
                                           :selectors [#:selector{:starts-with "abc"}]}])
        "type config maps must be namespaced"))

  (testing "actions config"
    (is (not (spec/valid? :config/actions []))
        "empty actions config is not allowed")

    (is (spec/valid? :config/actions [#:action{:name "action1"
                                               :exec "prog.exe"}
                                      #:action{:name "action2"
                                               :exec "prog.exe"}])
        "actions are valid")

    (is (not (spec/valid? :config/actions [#:action{:name "action1"}]))
        "key :exec is required")

    (is  (spec/valid? :config/actions [#:action{:name "action1"
                                                :exec "prog.exe"
                                                :args "scalar"}])
         "key :arg can be a seq")
    
    (is  (spec/valid? :config/actions [#:action{:name "action1"
                                                :exec "prog.exe"
                                                :args ["string" true 3.14]}])
         "key :arg can be a list")
    ))

(deftest add-ns-to-user-config-test
  (testing "adding namespace to user config map"
    (is (= "value" (:config/key (conf/add-ns-to-user-config {:key "value"})))
        "namespace 'config' is added to top key")

    (is (every? #(= "config" (namespace (first %)))
                (conf/add-ns-to-user-config {:key1 "value1"
                                             :key2 "value2"}))
        "namespace 'config' is added to all top level keys")

    (is (= #:config{:types
                    '(#:type{:name "type1", :selectors [#:selector{:equals "value"}]}
                      #:type{:name "type2", :selectors [#:selector{:ends-with "value"}]})}
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
                    :actions       '(#:action{:name "action1",
                                              :exec "notepad.exe",
                                              :args ("arg1")}
                                     #:action{:name "action2",
                                              :exec "notepad.exe no arg"}),

                    :types         '(#:type{:name "MY_FIRST_TYPE",
                                            :selectors (#:selector{:ends-with "md"})}
                                     #:type{:name "MY_SECOND_TYPE",
                                            :selectors (#:selector{:ends-with "bash"})
                                            :actions (#:action{:name "action1"})})}

           (conf/create-config "./test/back/fixtures/config_test-1.yaml"))
        "when file path is given, merge with default config")

    #_(is (thrown-with-msg? ExceptionInfo  #"Configuration file not found"
                            (conf/create-config "./not_found"))
          "throws when file not found")

    #_(is (thrown-with-msg? ExceptionInfo  #"Invalid User Configuration"
                            (conf/create-config "./test/back/fixtures/config_test-2.yaml"))
          "throws when invalid user configuration")
    #_(try
        (conf/create-config "./test/back/fixtures/config_test-2.yaml")
        (catch ExceptionInfo ex
          (let [error-info (ex-data ex)]
            (is (= "./test/back/fixtures/config_test-2.yaml" (:file error-info))
                "error info includes filename")
            (is  (:error error-info)
                 "error info includes reason")))))) 

