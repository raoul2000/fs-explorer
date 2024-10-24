(ns domain.explorer.metadata-test
  (:require [clojure.test :refer (deftest testing is use-fixtures)]
            [domain.explorer.metadata :as md]
            [babashka.fs :as fs]))


(deftest create-abs-path-test
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