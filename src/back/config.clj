(ns config
  "Manage app configuration"
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [clojure.walk :as w]
            [clojure.data.json :as json]
            [babashka.fs :as fs]
            [clojure.string :as s]))

;; spec ----------------------------------------------------------------------------------------------------------------

(spec/def :config/server-port    number?)
(spec/def :config/root-dir-path  string?)
(spec/def :config/open-browser   boolean?)
(spec/def :config/browse-url     string?)

(spec/def :config.type.selector/name keyword?)
(spec/def :config.type.selector/arg  string?)
(spec/def :config.type/selectors  (spec/every-kv :config.type.selector/name :config.type.selector/arg))

(spec/def :config.type/definition (spec/keys :req [:config.type/selectors]))
(spec/def :config/types (spec/every-kv keyword? :config.type/definition))

;; app config : key are required

(spec/def :config/map  (spec/keys :req [:config/server-port
                                        :config/root-dir-path
                                        :config/open-browser
                                        :config/browse-url]

                                  :opt [:config/types]))

;; conf settings provided by the user and dedicated to overloads default settings
;; all keys are optionals

(spec/def :user-config/map  (spec/keys :opt [:config/server-port
                                             :config/root-dir-path
                                             :config/open-browser
                                             :config/browse-url
                                             :config/types]))

;; default config --------------------------------------------------------------------------------------

(def default-port 8890)
(defn create-browse-url [port]
  (format "http://localhost:%d/" port))

(def default-config #:config{:server-port   default-port
                             :root-dir-path (str (fs/home))
                             :open-browser  true
                             :browse-url    (create-browse-url default-port)})

;; read user config from YAML/JSON file ----------------------------------------------------------------

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
            config-reader (io/reader    (fs/file abs-file-path))]

        (cond
          (#{"yml" "yaml"} ext)  (yaml/parse-stream config-reader)
          (= "json"        ext)  (json/read  config-reader :key-fn keyword)
          :else                  (throw (ex-info "File type not supported " {:ext ext}))))

      (catch Exception e
        (throw (ex-info "Failed to read configuration file" {:file (str abs-file-path)
                                                             :msg  (.getMessage e)}))))))

;; add namespace ---------------------------------------------------------------------------------

(defn add-ns-to-key [ns-name k]
  (keyword ns-name (name k)))

(defn add-ns-to-map
  "Add *ns-name* namespace to all keys of map *m*.
   When a key is a string it is converted into a keyword.
   When a key is a keyword already with a namespace, it is replaced by the given ns"
  [ns-name m]
  (into {} (map (fn [[k v]]
                  [(add-ns-to-key ns-name k) v]) m)))

(defn build-types [m]
  (into {} (map (fn [[k v]]
                  [k (add-ns-to-map "config.type" v)]) m)))

(defn add-ns-to-user-config
  "Given *m* a user config map with no namespace, returns a new map where keywords
   have been namespaced."
  [m]
  (w/walk (fn [[k v]]
            (let [ns-key (add-ns-to-key "config" k)]
              [ns-key (case ns-key
                        :config/types     (build-types   v)
                        v)])) identity m))

;; merge user config and default config -----------------------------------------------------

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

(def merge-config (partial  deep-merge-with (fn [_a b] b)))


(comment
  (deep-merge-with (fn [a b] b) {:a 1} {:a 5})
  (merge-config {:a 1 :b {:x1 [1 2 3] :x2 2}} {:a 5 :b {:x1 [11 22 33]}})
  ;;
  )

;; validate config --------------------------------------------------------------------------

(defn validate-config [config-spec m]
  (let [is-valid (spec/valid? config-spec m)
        result   {:config m
                  :is-valid is-valid}]
    (if-not is-valid
      (assoc result :error (spec/explain-data config-spec m))
      result)))

(def validate-user-config  (partial validate-config :user-config/map))
(def validate-final-config (partial validate-config :config/map))

(comment
  (def c1 (add-ns-to-user-config (read-from-file "./test/back/fixtures/config-1.yaml")))
  (def c2 (add-ns-to-user-config (read-from-file "./test/back/fixtures/config-1.json")))

  (spec/valid? :user-config/map
               (add-ns-to-user-config (read-from-file "./test/back/fixtures/config-1.yaml")))

  (spec/valid? :user-config/map
               (add-ns-to-user-config (read-from-file "./test/back/fixtures/config-1.json")))

  (validate-user-config c1)
  (validate-user-config c2)

  (validate-final-config c1)
  ;;
  )


;; create config -----------------------------------------------------------------------------

(defn create-config [user-config-file-path]
  ;; always validate default config
  (let [{:keys [error]} (validate-final-config default-config)]
    (when error
      (throw (ex-info "Internal Error : invalid default settings" {:default-settings default-config
                                                                   :error error}))))

  (let [user-config (when user-config-file-path
                      (-> user-config-file-path
                          read-from-file
                          add-ns-to-user-config))]
    (when user-config
      (let [{:keys [error]} (validate-user-config user-config)]
        (when error
          (throw (ex-info "Invalid User Configuration" {:file  user-config-file-path
                                                        :error error})))))

    ;; create final configuration and validate
    (let [final-config (if user-config
                         (merge-config default-config user-config)
                         default-config)
          validation   (validate-final-config final-config)]

      (when (:error validation)
        (throw (ex-info "Invalid Configuration" {:file  user-config-file-path
                                                 :error (:error validation)})))

      final-config)))

(comment

  (create-config nil)
  (create-config "./test/back/fixtures/config-1.yaml")
  (create-config "./test/back/fixtures/config-1.json")


  ;;
  )




