(ns user-config.normalize-test
  (:require [clojure.test :refer (deftest testing is)]
            [user-config.normalize :refer [parse]]))


(deftest parse-test
  (testing "Normalize a map into a user-config map"
    (is (= {}
           (parse {}))
        "empty map is a valid user-config")

    (is (= #:user-config{:server-port   222
                         :open-brwoser  true
                         :root-dir-path "/folder/folder"}
           (parse {"server-port"    222
                   "open-brwoser"   true
                   "root-dir-path"  "/folder/folder"}))
        "adds the user-config namespace")

    (is (= #:user-config{:server-port   222
                         :open-brwoser  true
                         :root-dir-path "/folder/folder"}
           (parse {"server-port   "    222
                   "   open-brwoser"   true
                   "  root-dir-path  "  "/folder/folder"}))
        "it trims top level keywords"))

  (testing "parse command index property"
    (is (= #:user-config{:command-index {"notepad" "notepad.exe"
                                         "command_id1" "command_line_1"}}
           (parse {"command-index" {"notepad" "notepad.exe"
                                    "command_id1" "command_line_1"}}))
        "when command is a string")

    (is (= #:user-config{:command-index {"notepad" {:command "notepad.exe"}}}
           (parse {"command-index" {"notepad" {"command" "notepad.exe"}}}))
        "when command is a map")

    (is (= #:user-config{:command-index {"notepad" {:command "notepad.exe"
                                                    :description "some description"}}}
           (parse {"command-index" {"notepad" {"command" "notepad.exe"
                                               "description" "some description"}}}))
        "when command is a map with a description"))

  (testing "parse type property"
    (is (= #:user-config{:type {"type_name" {:selector "filename.txt"}}}
           (parse {"type" {"type_name" {"selector" "filename.txt"}}}))
        "when type selector is a string"))) 




