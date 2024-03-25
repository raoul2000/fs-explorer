(ns server.domain.explorer.core-test
  (:require [clojure.test :refer (deftest testing is use-fixtures are)]
            [domain.explorer.core :as exp]
            [babashka.fs :as fs]))

;; fixtures ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


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


;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(deftest explore-test
  (testing "explore a file system tree"
    (is (= #:model{:content
                   [#:file{:name   "dir1",
                           :dir?   true,
                           :path   (str (fs/path fixture-base-path "dir1"))
                           :id     "dir1"
                           :action ""}

                    #:file{:name   "dir2",
                           :dir?   true,
                           :path   (str (fs/path fixture-base-path "dir2"))
                           :id     "dir2"
                           :action ""}]}
           (exp/explore (str fixture-base-path) {:root-dir-path fixture-base-path}))
        "returns 2 absolute path")))

(deftest absolutize-path-test
  (testing "dir and file path absolutizer"
    (are [result arg-map] (= result (str (exp/absolutize-path (:dir       arg-map)
                                                              (:root-path arg-map))))
      ""                              {:dir "" :root-path ""}
      (str (fs/normalize "/a/b/c"))   {:dir "" :root-path "/a/b/c"}
      (str (fs/normalize "/a/b/c"))   {:dir "c" :root-path "/a/b"}
      (str (fs/normalize "/a/b/c"))   {:dir "b/c" :root-path "/a"}

      ;; TODO: add more tests 
      ;;
      )))

(comment

  (fs/absolute? "c:\\a")
  (fs/normalize "a/b/c")
  (fs/absolutize "a/b/c")
  (exp/absolutize-path "a/b" "c:/tmp")

  ;;
  )
