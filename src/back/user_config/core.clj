(ns user-config.core
  (:require [babashka.fs :as fs]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]
            [utils :refer [can-be-converted-to-url?]]))

;; function  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- json-string->map
  "Converts the given JSON string into a map with namespaced keywords. Throws
   when invalid JSON"
  [s]
  (json/read-str s :key-fn #(keyword "user-config" %)))

(comment
  (def cfg-1 (json-string->map "{\"server-port\" : 45}"))
  (s/valid? :user-config/config cfg-1)

  (def cfg-2 (json-string->map "{\"server-port\" : -45}"))

  (s/valid? :user-config/config cfg-2)
  (s/explain-str :user-config/config cfg-2)

  ;;
  )

(defn- read-from-file
  "Read the given *file-path* and parse its JSON content to the returned map.
   Throws on error."
  [file-path]
  (let [abs-file-path (fs/absolutize file-path)]
    (when-not (fs/regular-file? abs-file-path)
      (throw (ex-info "Configuration file not found" {:file (str abs-file-path)})))

    (try
      (println (format "Loading user configuration from %s ..." abs-file-path))
      (json-string->map (slurp (fs/file abs-file-path)))
      (catch Exception e
        (throw (ex-info "Failed to parse JSON configuration file" {:file (str abs-file-path)
                                                                   :msg  (.getMessage e)}))))))

(defn- validate
  "Returns the given *user-config* map if it is valid or throws"
  [{browse-url    :user-config/browse-url
    root-dir-path :user-config/root-dir-path
    :as           user-config}]

  (cond
    (not (s/valid? :user-config/config user-config))
    (throw (ex-info "User configuration is not valid" {:msg (s/explain-str :user-config/config user-config)}))

    (and browse-url
         (not (can-be-converted-to-url? (:user-config/browse-url user-config))))
    (throw (ex-info "Invalid URL" {:browse-url (:user-config/browse-url user-config)}))

    (and root-dir-path
         (not (and (fs/directory? (:user-config/root-dir-path user-config))
                   (fs/absolute?  (:user-config/root-dir-path user-config)))))
    (throw (ex-info "Invalid Dir" {:dir (:user-config/root-dir-path user-config)}))

    :else user-config))


(defn load-from-file
  "Read user config from the given *file-path* and validate it. Returns
   the user config map or throws.
   
   All keys in the returned map are namespaced in 'user-config'. 

   Example : 
   ```
   {
      :user-config/open-browser true
      :user-config/server-port  8001
   }
   ```
   The returned map is valid for the :user-config/config spec
   "
  [file-path]
  (->> file-path
       read-from-file
       validate))


