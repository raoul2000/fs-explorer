(ns domain.explorer.metadata-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [domain.explorer.metadata :as md]
            [babashka.fs :as fs]
            [clojure.data.json :as json]
            [clj-yaml.core :as yaml])
  (:import [clojure.lang ExceptionInfo]))


(def fixture-base-path  (fs/absolutize "test/fixture/metadata"))
(def file-1             (fs/path fixture-base-path "dir1" "dir2" "file-1.txt"))
(def file-1-metadata    (fs/path fixture-base-path "dir1" "dir2" "file-1.txt.meta"))
(def file-1-md-content  {:prop "value"})

(def file-2             (fs/path fixture-base-path "dir1" "dir2" "file-2.txt"))
(def file-2-metadata    (fs/path fixture-base-path "dir1" "dir2" "file-2.txt.mdinfo"))
(def file-2-md-content  {:prop "value"})

(def file-3             (fs/path fixture-base-path "dir1" "dir2" "file-3.txt"))
(def file-3-metadata    (fs/path fixture-base-path "dir1" "dir2" "file-3.txt.json.meta"))
(def file-3-md-content  {:prop "JSON value"})

(def file-4             (fs/path fixture-base-path "dir1" "dir2" "file-4.txt"))
(def file-4-metadata    (fs/path fixture-base-path "dir1" "dir2" "file-4.txt.yaml.meta"))
(def file-4-md-content  {:prop "YAML value"})

(def file-5                 (fs/path fixture-base-path "dir1" "dir2" "file-5.txt"))
(def file-5-metadata-json   (fs/path fixture-base-path "dir1" "dir2" "file-5.txt.json.meta"))
(def file-5-metadata-yaml   (fs/path fixture-base-path "dir1" "dir2" "file-5.txt.yaml.meta"))
(def file-5-md-content-json {:prop "JSON value"})
(def file-5-md-content-yaml {:prop "YAML value"})

(defn create []
  (fs/create-dirs (fs/path fixture-base-path "dir1/dir2"))

  (fs/create-file file-1)
  (fs/create-file file-1-metadata)
  (spit (fs/file file-1-metadata) (json/write-str file-1-md-content))

  (fs/create-file file-2)
  (fs/create-file file-2-metadata)
  (spit (fs/file file-2-metadata) (yaml/generate-string file-2-md-content))

  (fs/create-file file-3)
  (fs/create-file file-3-metadata)
  (spit (fs/file file-3-metadata) (json/write-str file-3-md-content))

  (fs/create-file file-4)
  (fs/create-file file-4-metadata)
  (spit (fs/file file-4-metadata) (yaml/generate-string file-4-md-content))

  (fs/create-file file-5)
  (fs/create-file file-5-metadata-yaml)
  (spit (fs/file file-5-metadata-yaml) (yaml/generate-string file-5-md-content-yaml))
  (fs/create-file file-5-metadata-json)
  (spit (fs/file file-5-metadata-json) (json/write-str file-5-md-content-json)))

(defn with-metadata [f]
  (create)
  (f)
  (fs/delete-tree fixture-base-path))

(use-fixtures :once with-metadata)

(deftest format-token-test
  (testing "create format token info"
    (is (= [{:token "", :format :json}]
           (md/format-token "json")))

    (is (= [{:token "", :format :yaml}]
           (md/format-token "yaml")))

    (is (= [{:token ".json", :format :json}
            {:token ".yaml", :format :yaml}]
           (md/format-token "mixed")))

    (is (thrown-with-msg? ExceptionInfo  #"invalid metadata format"
                          (md/format-token "not_found"))
        "throw when the format is invalid")))


(deftest file-path-token-test
  (testing "create file path token"
    (is (= "c:\\tmp\\file.txt"
           (md/file-path-token #:file{:path "c:\\tmp\\file.txt"
                                      :dir?  false})))

    (is (= (str "c:\\tmp\\folder" fs/file-separator)
           (md/file-path-token #:file{:path "c:\\tmp\\folder"
                                      :dir?  true})))))

(deftest create-metadata-candidates-test
  (testing "Create map describing potential metadata files"
    (is (= [{:token  ""
             :format :json
             :path   (str "/dir1/dir2" fs/file-separator  ".metaext")}]
           (md/create-metadata-candidates #:file{:path "/dir1/dir2"
                                                 :dir?  true} "json" "metaext"))
        "returns single item in seq describing a json metadata file for a folder")

    (is (= [{:token   ""
             :format  :yaml
             :path    "/dir1/file.txt.meta"}]
           (md/create-metadata-candidates #:file{:path "/dir1/file.txt"
                                                 :dir?  false} "yaml" "meta"))
        "returns single item describing a yaml metadata file for a regular file")

    (is (thrown-with-msg? ExceptionInfo  #"invalid metadata format"
                          (md/create-metadata-candidates #:file{:path "dir1/dir2"
                                                                :dir? true} "invalid_format" "meta"))
        "throws when format is invalid")))

(deftest read-metadata-test
  (testing "Read metadata file related to a given file item"
    (is (= {:prop "value"}
           (:file/metadata (md/read-metadata #:metadata{:format         "json"
                                                        :file-extension "meta"} #:file{:path file-1
                                                                                       :dir? false})))
        "returns metadata from JSON file for file related metadata")

    (is (= {:prop "value"}
           (:file/metadata (md/read-metadata #:metadata{:format         "yaml"
                                                        :file-extension "mdinfo"}  #:file{:path file-2
                                                                                          :dir? false})))
        "returns metadata from YAML file for file related metadata")

    (is (= {:prop "JSON value"}
           (:file/metadata (md/read-metadata #:metadata{:format         "mixed"
                                                        :file-extension "meta"}  #:file{:path file-3
                                                                                        :dir? false})))
        "guess metadata from mixed (json) file for file related metadata")

    (is (= {:prop "YAML value"}
           (:file/metadata (md/read-metadata #:metadata{:format         "mixed"
                                                        :file-extension "meta"} #:file{:path file-4
                                                                                       :dir? false})))
        "guess  metadata from mixed (json) file for file related metadata")

    (is (= {:prop "JSON value"}
           (:file/metadata (md/read-metadata  #:metadata{:format         "mixed"
                                                         :file-extension "meta"} #:file{:path file-5
                                                                                        :dir? false})))
        "when both format exist, use JSON")))



