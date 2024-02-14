(ns server.domain.explorer-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [domain.explorer :refer (explore)]
            [babashka.fs :as fs]))

(def fixture-base-path  (fs/absolutize "test/back/fixtures"))
(defn make-dirs [dirs]
  (fs/create-dirs (fs/path fixture-base-path dirs)))

(defn create-fs []
  (make-dirs "dir1/dir2-1")

  (fs/create-dirs (fs/path fixture-base-path "dir1/dir2-1"))
  (spit (fs/file (fs/path fixture-base-path "dir1/file.txt")) "some content")
  (fs/create-dirs (fs/path fixture-base-path "dir1/dir2-2"))
  (fs/create-dirs (fs/path fixture-base-path "dir1/dir2-3"))
  (fs/create-dirs (fs/path fixture-base-path "dir2/dir2-1"))
  (fs/create-dirs (fs/path fixture-base-path "dir2/dir2-2")))

(defn clean-fs []
  (fs/delete-tree fixture-base-path))

(defn fixture-for-fs [f]
  (create-fs)
  (f)
  (clean-fs))

(use-fixtures :once fixture-for-fs)

(deftest explore-test
  (testing "explore a file system tree"

    (is (= {:content  [(str (fs/path fixture-base-path "dir1"))
                       (str (fs/path fixture-base-path "dir2"))]}
           (explore fixture-base-path)))))


#_(deftest say-hello-test
    (testing "when saying hello"
      (is (= "Good morning joe !"
             (say-hello "joe" true)))

      (is (= "Hi joe !"
             (say-hello "joe" false))
          "saying hello not polite")

      (is (thrown? Exception (say-hello "bob" false))
          "error for 'bob' not polite")

      (is (thrown? Exception (say-hello "bob" true))
          "error for 'bob' being polite"))) 