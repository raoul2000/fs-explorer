(ns domain.explorer
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [model :as model]))


(defn- read-file-content [file-path]
  {:model/content (slurp file-path)})

(defn- list-dir-content [dir-path]
  {:model/content (->> (fs/list-dir dir-path)
                       (map str))})

(defn explore [fs-path]
  {:post [(s/valid? :model/read-result %)]}
  (if (fs/regular-file? fs-path)
    (read-file-content fs-path)
    (list-dir-content fs-path)))

(comment
  (explore "c:\\tmp")
  (->> (fs/list-dir "c:\\tmp")
       (map str))
  ;;
  )