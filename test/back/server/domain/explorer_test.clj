(ns server.domain.explorer-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [domain.explorer :refer (explore)]
            [babashka.fs :as fs]))

(def fixture-base-path  (fs/absolutize "test/back/fixtures/fs"))
(defn make-dirs [dirs]
  (doseq [dir dirs]
    (fs/create-dirs (fs/path fixture-base-path dir))))

(defn create-fs []
  (make-dirs ["dir1/dir2-1"
              "dir1/dir2-2"
              "dir1/dir2-3"
              "dir2/dir2-1"]))

(defn clean-fs []
  (fs/delete-tree fixture-base-path))

(defn with-fs-tree [f]
  (create-fs)
  (f)
  (clean-fs))

(use-fixtures :once with-fs-tree)

(deftest explore-test
  (testing "explore a file system tree"

    (is (= {:model/content  [(str (fs/path fixture-base-path "dir1"))
                             (str (fs/path fixture-base-path "dir2"))]}
           (explore fixture-base-path))
        "returns 2 absolute path")))