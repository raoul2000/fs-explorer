(ns user-config
  "User configuration loading and validation"
  (:require [babashka.fs :as fs]
            [clojure.data.json :as json]
            [clojure.spec.alpha :as s]))

(s/def ::server-port    (s/and int? #(< 0 % 65353)))
(s/def ::open-browser   boolean?)
(s/def ::browse-url     (s/and string?
                               #(try
                                  (new java.net.URL %)
                                  (catch Throwable _t false))))

(s/def ::config         (s/keys :opt [::server-port
                                      ::open-browser
                                      ::browse-url]))

(comment
  (new java.net.URI "http:dd.com:888")

  (s/valid? ::browse-url "https://rlo.caelhost:8808/?ee=zz")
  (s/valid? ::browse-url "")
  (s/explain-data ::browse-url "http://localhost:8808")
  (s/valid? ::server-port 112)
  (s/valid? ::server-port -112)
  (s/conform ::server-port 112)
  (s/conform ::server-port "112")
  (s/explain ::server-port "112")

  (s/valid? ::config {})
  (s/valid? ::config {::server-port 11})
  (s/valid? ::config {:server-port -11})
  (s/valid? ::config {::server-port "XX"})
  (s/explain-data  ::config {::server-port "XX"})
  ;;
  )

(defn- json-string->map
  "Converts the given JSON string into a map with namespaced keywords. Throws
   when invalid JSON"
  [s]

  (json/read-str s :key-fn #(keyword #_(str *ns*)  "user-config" %)))

(comment
  (def cfg-1 (json-string->map "{\"server-port\" : 45}"))
  (s/valid? ::config cfg-1)

  (def cfg-2 (json-string->map "{\"server-port\" : -45}"))

  (s/valid? ::config cfg-2)
  (s/explain-str ::config cfg-2)

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
  [user-config]
  (when-not (s/valid? ::config user-config)
    (throw (ex-info "User configuration is not valid" {:msg (s/explain-str ::config user-config)})))
  user-config)

(comment

  (s/explain ::config {:user-config/server-port 888
                       :otherk "Val"})

  (try
    (validate {:user-config/server-port -888})
    (catch Exception e
      (println (format "Error : %s - cause : %s" (.getMessage e) (ex-data e)))))

  ;;
  )

(defn load-from-file
  "Read user config from the given *file-path* and validate it. Returns
   the user config map or throws."
  [file-path]
  (->> file-path
       read-from-file
       validate))

(comment
  (s/conform ::config (read-from-file "test/back/fixture/config-ok.json"))
  (try
    (load-from-file "test/back/fixture/config-invalid-port.json")
    (catch Exception e
      (println (format "Error : %s - cause : %s" (.getMessage e) (ex-data e)))))

  ;;
  )
