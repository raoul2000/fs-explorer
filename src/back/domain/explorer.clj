(ns domain.explorer
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [model :as model]))

(defn- read-file-content [file-path]
  {:model/content (slurp file-path)})

(defn- list-dir-content [dir-path]
  {:model/content (->> (fs/list-dir dir-path)
                       (map (fn [file]
                              {:file/name (fs/file-name file)
                               :file/dir? (fs/directory? file)
                               :file/path (str file)})))})

(defn explore [fs-path]
  {:post [(s/valid? :model/read-result %)]}
  (if (fs/regular-file? fs-path)
    (read-file-content fs-path)
    (list-dir-content fs-path)))

(comment
  (s/valid? :model/read-result (explore "c:\\tmp"))
  (s/explain-data :model/read-result (explore "c:\\tmp"))
  (->> (fs/list-dir "c:\\tmp")
       (map str))
  ;;
  )