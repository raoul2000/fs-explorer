(ns domain.explorer.metadata-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [domain.explorer.metadata :as md]
            [babashka.fs :as fs])
  (:import [clojure.lang ExceptionInfo]))

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

#_(deftest create-abs-path-test
    (testing "create metadata file absolute path"
      (is (= "c:\\tmp\\file.txt.meta"
             (md/create-abs-path "c:\\tmp\\file.txt" "json" "meta")))

      (is (= "c:\\tmp\\file.txt.meta"
             (md/create-abs-path "c:\\tmp\\file.txt" "yaml" "meta")))

      (is (= "c:\\tmp\\file.txt.metaext"
             (md/create-abs-path "c:\\tmp\\file.txt" "json" "metaext")))

      (is (= "c:\\tmp\\file.txt.metaext"
             (md/create-abs-path "c:\\tmp\\file.txt" "yaml" "metaext")))

      (is (= "c:\\tmp\\file.json.metaext"
             (md/create-abs-path "c:\\tmp\\file.txt" "mixed" "metaext")))

      (is (= "c:\\tmp\\file.yaml.metaext"
             (md/create-abs-path "c:\\tmp\\file.txt" "mixed" "metaext"))))) 