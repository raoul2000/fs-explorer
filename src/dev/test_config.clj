(ns test-config
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]
            [clojure.walk :as w]
            [clojure.data.json :as json]))

;; config is a map built by merging default config settings and, when available, user defined settings
;; - User defined settings overload default settings
;; - User defined settings are provided as a YAML file

;; Config map must be validated by specs

(spec/def :config/server-port    number?)
(spec/def :config/root-dir-path  string?)
(spec/def :config/open-browser   boolean?)
(spec/def :config/browse-url     string?)


(spec/def :config.type.selector/name keyword?)
(spec/def :config.type.selector/arg  string?)
(spec/def :config.type/selectors  (spec/every-kv :config.type.selector/name :config.type.selector/arg))

(spec/def :config.type/definition (spec/keys :req [:config.type/selectors]))
(spec/def :config/types (spec/every-kv keyword? :config.type/definition))

(spec/def :config/map  (spec/keys :req [:config/server-port
                                        :config/root-dir-path
                                        :config/open-browser
                                        :config/browse-url]

                                  :opt [:config/types]))

;; conf settings provided by the user and dedicated to overloads default settings
(spec/def :user-config/map  (spec/keys :opt [:config/server-port
                                             :config/root-dir-path
                                             :config/open-browser
                                             :config/browse-url
                                             :config/types]))

(comment
  (def config-1 #:config{:server-port 12
                         :root-dir-path "/tmp"
                         :open-browser true
                         :browse-url "http://localhost"
                         :types  {:MY_FIRST_TYPE #:config.type{:selectors {:pred  "string"}}
                                  :MY_SECOND_TYPE #:config.type{:selectors {:pred  "string"
                                                                            :other-pred "arg2"}}}})
  (spec/valid? :config/map config-1)
  (spec/valid? :user-config/map {})

  ;; reading config values using namespaced keys : 

  (:config/server-port config-1)
  (get-in config-1 [:config/types :MY_FIRST_TYPE :config.type/selectors])
  ;; all declared types (as keywords)
  (keys (:config/types config-1))

  ;; all declared types (as strings)
  (map name (keys (:config/types config-1)))

  ;;
  )


;; loading and prasing user config as YAML file into a map

(def conf (yaml/parse-stream (io/reader "./test/back/fixtures/config-1.yaml")))

;; ... but the map returned does not include any namespace : we must add them
;; Let's create function for this 

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

(defn add-ns-to-user-config [m]
  (w/walk (fn [[k v]]
            (let [ns-key (add-ns-to-key "user-config" k)]
              [ns-key (case ns-key
                        :config/types     (build-types   v)
                        v)])) identity m))

(comment

  (spec/valid?  :user-config/map (add-ns-to-user-config conf))
  (spec/explain :user-config/map (add-ns-to-user-config conf))

  ;; now turn the config map into json
  ;; does not preserve namespace
  (print (json/write-str (add-ns-to-user-config conf) :escape-slash false))

  ;; using juxt ?

  (into {}
        (map (juxt  (comp (partial add-ns-to-key "my-ns") first) second)
             {:a 1
              :b 2}))
  ;; ok

  ;;
  )


(comment
  ;; refactor / improve map ns adding to config map
  ;; use update-in to add namespace on deeply nested maps

  (def m1 {:browse-url "http://hostname"
           :actions [{:name "notepad"
                      :exec "notepad.exe"}
                     {:name "photoshop"
                      :exec "c:\\programs\\photoshop.exe"}]
           :types [{:name      "type1"
                    :selectors [{:starts-with "file"}
                                {:ends-with "txt"}]
                    :actions    [{:name "notepad"}]}]})

  (defn add-ns-to-map
    "Add *ns-name* namespace to all keys of map *m*.
     When a key is a string it is converted into a keyword.
     When a key is a keyword already with a namespace, it is replaced by the given ns"
    [m ns-name]
    (into {} (map (fn [[k v]]
                    [(add-ns-to-key ns-name k) v]) m)))

  (update-in m1 [:actions] (fn [actions]
                             (map #(add-ns-to-map % "action") actions)))

  (update-in m1 [:types] (fn [actions]
                           (map #(add-ns-to-map % "type") actions)))

  (defn add-config-ns [m]
    (add-ns-to-map m "config"))

  (defn add-type-action-ns [m]
    (add-ns-to-map m "action"))
  
  (defn add-type-ns [k m]
    (update m k (fn [type-m]
                  (map #(add-ns-to-map % "type") type-m))))

  (defn add-action-ns [k m]
    (update m k (fn [action-m]
                  (map #(add-ns-to-map % "action") action-m))))

  (->> m1
       add-config-ns
       (add-type-ns   :config/types)
       (add-action-ns :config/actions))


  ;;
  )

(comment
  ;; Let's review the merging process between default config and user defined config

  (def default-config #:config{:server-port 8880
                               :root-dir-path "/tmp"
                               :open-browser true
                               :browse-url "http://localhost"
                               :types  {:file #:config.type{:selectors {:is-file  "string"}}
                                        :folder #:config.type{:selectors {:is-folder  "string"}}}})

  ;; of course, default config should be valid
  (spec/valid? :config/map default-config)

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

  ;; let's create a partial user-config
  (def config-2 #:user-config{:root-dir-path "/my/default/folder"
                              :open-browser true
                              :browse-url "http://localhost"
                              :types  {:MY_FIRST_TYPE #:config.type{:selectors {:pred  "string"}}}})

  (def user-config (add-ns-to-user-config config-2))
  ;; and it should be valid
  (spec/valid? :user-config/map user-config)

  (def final-config (deep-merge-with  (fn [a b] b) default-config  user-config))

  ;; the merged (final) config must also be valid
  (spec/valid? :config/map final-config)

  (= 8880 (:config/server-port final-config))
  (= "/tmp" (:config/root-dir-path final-config))

  ;;
  )

