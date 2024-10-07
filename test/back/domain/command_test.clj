(ns domain.command-test
  (:require [clojure.test :refer (deftest testing is are)]
            [domain.action :as cmd]))

(deftest create-args-vec-test
  (testing "Creating arguments vector"
    (is (= ["exe" "arg1" "/absolute/path/to/file.txt"]
           (cmd/create-args-vec #:action{:name "action"
                                         :exec "exe"
                                         :args "arg1"}
                                "/absolute/path/to/file.txt"))
        "creates a vector for arguments when args is a string")

    (is (= ["exe" "arg1" "arg2" "arg3" "/absolute/path/to/file.txt"]
           (cmd/create-args-vec #:action{:name "action"
                                         :exec "exe"
                                         :args ["arg1" "arg2" "arg3"]}
                                "/absolute/path/to/file.txt"))
        "creates a vector for arguments when args is a vector")

    (is (= ["exe" "/absolute/path/to/file.txt"]
           (cmd/create-args-vec #:action{:name "action"
                                         :exec "exe"}
                                "/absolute/path/to/file.txt"))
        "creates a vector for arguments when args is nil")

    (is (= ["exe" "/absolute/path/to/file.txt"]
           (cmd/create-args-vec #:action{:name "action"
                                         :exec "exe"
                                         :args cmd/abs-path-placeholder}
                                "/absolute/path/to/file.txt"))
        "interpolate abs path when args is a string")

    (is (= ["exe" "arg1" "/absolute/path/to/file.txt" "arg2"]
           (cmd/create-args-vec #:action{:name "action"
                                         :exec "exe"
                                         :args ["arg1" cmd/abs-path-placeholder "arg2"]}
                                "/absolute/path/to/file.txt"))
        "interpolate abs path when args is a vector")

    (is (= ["exe" "arg1" "/absolute/path/to/file.txt" "arg2" "/absolute/path/to/file.txt" "arg3"]
           (cmd/create-args-vec #:action{:name "action"
                                         :exec "exe"
                                         :args ["arg1" cmd/abs-path-placeholder "arg2" cmd/abs-path-placeholder "arg3"]}
                                "/absolute/path/to/file.txt"))
        "interpolate all placholders")))
