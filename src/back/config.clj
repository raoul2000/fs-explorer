(ns config
  "Manage app configuration creation.
   
   Loads config from file (if given), validate, marge with default config, validate and return
   "
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.data.json :as json]
            [babashka.fs :as fs]
            [clojure.string :as s]))

;; spec ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn can-be-converted-to-url?
  "Returns TRUE if *s* can be converted into a java.net.URL object"
  [s]
  (try
    (new java.net.URL s)
    true
    (catch Throwable _t false)))

(spec/def :string/not-blank           (spec/and string? (complement s/blank?)))
(spec/def :coll/non-empty-string-list (spec/coll-of :string/not-blank :min-count 1))

(spec/def :action/name  :string/not-blank)
(spec/def :action/exec  :string/not-blank)
(spec/def :action/args  (spec/coll-of (spec/or :string  string?
                                               :number  number?
                                               :boolean boolean?)
                                      :min-count 1))
(spec/def :action/def   (spec/keys :req [:action/name
                                         :action/exec]
                                   :opt [:action/args]))

(spec/def :selector/starts-with :string/not-blank)
(spec/def :selector/ends-with   :string/not-blank)
(spec/def :selector/equals      :string/not-blank)
(spec/def :selector/def         (spec/keys :req [(or :selector/starts-with
                                                     :selector/ends-with
                                                     :selector/equals)]))

(spec/def :type/name       :string/not-blank)
(spec/def :type/selectors  (spec/coll-of :selector/def :min-count 1))
(spec/def :type/action-ref (spec/keys :req [:action/name]))
(spec/def :type/actions    (spec/coll-of  :type/action-ref :min-count 1))
(spec/def :type/def        (spec/keys :req [:type/name
                                            :type/selectors]
                                      :opt [:type/actions]))

(spec/def :config/server-port    (spec/and int? #(< 0 % 65353)))
(spec/def :config/root-dir-path  string?)
(spec/def :config/open-browser   boolean?)
(spec/def :config/browse-url     can-be-converted-to-url?)
(spec/def :config/types          (spec/coll-of :type/def   :min-count 1))
(spec/def :config/actions        (spec/coll-of :action/def :min-count 1))

(spec/def :config/map            (spec/keys :req [:config/server-port
                                                  :config/root-dir-path
                                                  :config/open-browser
                                                  :config/browse-url]
                                            :opt [:config/types
                                                  :config/actions]))

(spec/def :user-config/map       (spec/keys :opt [:config/server-port
                                                  :config/root-dir-path
                                                  :config/open-browser
                                                  :config/browse-url
                                                  :config/types
                                                  :config/actions]))

;; default config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def default-port 8890)
(defn create-browse-url [port]
  (format "http://localhost:%d/" port))

(def default-config #:config{:server-port   default-port
                             :root-dir-path (str (fs/home))
                             :open-browser  true
                             :browse-url    (create-browse-url default-port)})

;; getters ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn server-port   [config] (:config/server-port   config))
(defn root-dir-path [config] (:config/root-dir-path config))
(defn open-broser?  [config] (:config/open-browser  config))
(defn browse-url    [config] (:config/browse-url    config))

(defn types-definition   [config] (:config/types    config))
(defn actions-definition [config] (:config/actions    config))


;; read user config from YAML/JSON file ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- read-from-file
  "Read the given *file-path* and parse its YAML or JSON content to the returned map.
   Throws on error."
  [file-path]
  (let [abs-file-path (fs/absolutize file-path)]
    (when-not (fs/regular-file? abs-file-path)
      (throw (ex-info "Configuration file not found" {:file (str abs-file-path)})))

    (try
      (println (format "Loading user configuration from %s ..." abs-file-path))

      (let [ext           (s/lower-case (fs/extension abs-file-path))
            config-reader (io/reader    (fs/file      abs-file-path))]

        (cond
          (#{"yml" "yaml"} ext)  (yaml/parse-stream config-reader)
          (= "json"        ext)  (json/read         config-reader :key-fn keyword)
          :else                  (throw (ex-info "File type not supported " {:ext ext}))))

      (catch Exception e
        (throw (ex-info "Failed to read configuration file" {:file (str abs-file-path)
                                                             :msg  (.getMessage e)}))))))

;; add namespace ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn add-ns [ns-name k]
  (keyword ns-name (name k)))

(defn process-type-selectors [selectors-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "selector" k) v)) %)) selectors-xs))

(defn process-type-actions [actions-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "action" k) v)) %)) actions-xs))

(defn process-config-types [types-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "type" k)
                                (case k
                                  :selectors (process-type-selectors v)
                                  :actions   (process-type-actions v)
                                  v))) %)) types-xs))

(defn process-config-actions [actions-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "action" k) v)) %)) actions-xs))

(defn add-ns-to-user-config
  "Given *m* a user config map with no namespace, returns a new map where keywords
   have been namespaced."
  [m]
  (into {} (map (fn [[k v]]
                  (vector (add-ns "config" k)
                          (case k
                            :types   (process-config-types v)
                            :actions (process-config-actions v)
                            v))) m)))

;; merge user config and default config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; see https://clojure.github.io/clojure-contrib/map-utils-api.html#clojure.contrib.map-utils/deep-merge-with
(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.
  ```
  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}
  ```
     "
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))

(defn merge-configs
  "Merge default config with possibly *nil* user config map and returns the result.
   
   When the user config defines a *server-port* value but no *browse-url* value, the default
   *browse-url* value is updated with the user *server-port* value.
   
   Returns *default-m* when *user-m* is nil"
  [default-m user-m]
  (if user-m
    (cond-> (deep-merge-with (fn [_a b] b) default-m user-m)
      (and (:config/server-port user-m)
           (nil? (:config/browse-url user-m))) (assoc :config/browse-url (create-browse-url (:config/server-port user-m))))
    default-m))

(comment

  (merge-configs #:config{:server-port 2
                          :browse-url "http://localhost:22"}
                 #:config{:server-port 33})

  (merge-configs #:config{:server-port 2
                          :browse-url "http://localhost:22"}
                 #:config{:browse-url "http://HOST:8888"})

  (merge-configs #:config{:server-port 2
                          :browse-url "http://localhost:22"}
                 #:config{:open-browser? true
                          :browse-url "http://localhost:777"})
  ;;
  )

;; validate config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn config-error
  "Validate *m* against spec *config-spec* and returns explain when not valid otherwise returns *nil*"
  [config-spec m]
  (when-not (spec/valid? config-spec m)
    (spec/explain-data config-spec m)))

(def user-config-error    (partial config-error :user-config/map))
(def default-config-error (partial config-error :config/map))
(def final-config-error   default-config-error)

(comment

  (config-error :config/map default-config)
  (config-error :config/map {})
 ;;
  )


;; create config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-config
  "Creates and return a valid configuration map.
   
   When *user-config-file-path* is not nil, read user configuration from the file and merges it with the
   default configuration.

   The returned configuration map is valid against spec `:config/map`.

   Throws on error.
   "
  [user-config-file-path]
  ;; always validate default config
  (when-let [validation-error (default-config-error default-config)]
    (throw (ex-info "Internal Error : invalid default settings" {:default-settings default-config
                                                                 :error            validation-error})))

  (let [user-config (when user-config-file-path
                      (-> user-config-file-path
                          read-from-file
                          add-ns-to-user-config))]
    (when user-config
      (when-let [validation-error (user-config-error user-config)]
        (print validation-error)
        (throw (ex-info "Invalid User Configuration" {:file  user-config-file-path
                                                      :error validation-error}))))

    ;; create final configuration and validate
    (let [final-config (merge-configs default-config user-config)]
      (when-let [validation-error (final-config-error final-config)]
        (throw (ex-info "Invalid Configuration" {:file  user-config-file-path
                                                 :error validation-error})))
      final-config)))

(comment

  (def cfg (read-from-file "./test/back/fixtures/config-1.yaml"))
  (def cfg (read-from-file "./test/back/fixtures/config_test-1.yaml"))

  (def cfg-ns (add-ns-to-user-config cfg))

  (:config/types cfg-ns)
  (-> (get-in cfg-ns [:config/types])
      second
      :config.type/actions
      first
      :name)

  (spec/valid? :user-config/map cfg-ns)
  (spec/explain :user-config/map cfg-ns)
  ;; success because all ns keys are optionals

  (spec/valid? :config/map cfg-ns)
  (spec/explain :config/map  cfg-ns)
  ;; fails because keys are not namespaced 

  (server-port cfg)
  (root-dir-path cfg)

  ;;
  )
