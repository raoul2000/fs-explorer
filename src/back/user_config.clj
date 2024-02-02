(ns user-config
  (:require [babashka.fs :as fs]
            [clojure.data.json :as json]))


(defn- json-string->map [s]
  (json/read-str s :key-fn keyword))


(defn- read-from-file [file-path]
  (let [abs-file-path (fs/absolutize file-path)]
    (when-not (fs/regular-file? abs-file-path)
      (throw (ex-info "Configuration file not found" {:file (str abs-file-path)})))

    (try
      (json-string->map (slurp (fs/file abs-file-path)))
      (println (format "User configuration loaded from file %s" abs-file-path))
      (catch Exception e
        (throw (ex-info "Failed to parse JSON configuration file" {:file (str abs-file-path)
                                                                   :msg  (.getMessage e)}))))))

(defn- validate [user-config]
  user-config
  ;; TODO: implement me !
  )

(defn load 
  "Read user config from the given *file-path* and validate it. Returns
   the user config map or throws" 
  [file-path]
  (->> file-path
       read-from-file
       validate))
