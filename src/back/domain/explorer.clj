(ns domain.explorer
  (:require [babashka.fs :as fs]))


(defn- read-file-content [file-path]
  {:content (slurp file-path)})

(defn- list-dir-content [dir-path]
  {:content (->> (fs/list-dir dir-path)
                 (map str))})

(defn explore [fs-path]
  (if (fs/regular-file? fs-path)
    (read-file-content fs-path)
    (list-dir-content fs-path)))


(comment

  (->> (fs/list-dir "c:\\tmp")
       (map str))
  ;;
  )