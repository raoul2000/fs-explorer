(ns domain.open-file
  (:require [babashka.fs :as fs]
            [domain.explorer.core :refer [absolutize-path]])
  (:import (java.io File)
           (java.awt Desktop)))

(defn- open-in-default-app
  "Opens f (a string) in the default application registered by the os.  May not
  work on all platforms.  Returns f on success, nil on failure."
  [^String f]
  (try
    (when (Desktop/isDesktopSupported)
      (. (.  Desktop getDesktop) open (File. f)))
    f
    (catch Exception e (throw (ex-info "failed to open file " {:file f
                                                               :cause (.getMessage e)})))))

(defn open [file-path root-dir-path]
  (let [abs-path (absolutize-path (or file-path "") root-dir-path)]
    (when-not (fs/regular-file? abs-path)
      (throw (ex-info "file not found" {:file-path  file-path
                                        :root-path  root-dir-path
                                        :abs-path   abs-path})))
    (open-in-default-app abs-path)
    {:success true}))
