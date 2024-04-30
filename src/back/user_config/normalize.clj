(ns user-config.normalize
  "This module is dedicated to user-config map conversion"
  (:require [clojure.spec.alpha :as s]
            [clojure.string :refer [trim]]
            [clojure.walk :as w]))


(defn build-command-instruction [instr]
  (cond->> instr
    (map? instr)  (w/walk #(vector (keyword (first %)) (second %)) identity)))

(defn build-command-map [m]
  (w/walk (fn [[k v]]
            [k (build-command-instruction v)])  identity m))

(defn build-type-value [value]
  (cond->> value
    (map? value)  (w/walk #(vector (keyword (first %)) (second %)) identity)))

(defn build-type-map [m]
  (w/walk (fn [[k v]]
            [k (build-type-value v)])  identity m))

(defn parse
  "Given map *m* assumed to be a representation of the user configuration obtained
   by parsing a JSON file, returns a map that conforms to :user-config/config spec"
  [m]
  (w/walk  (fn [[k v]]
             (cond
               (= "command-index" k) [(keyword "user-config" (trim k)) (build-command-map v)]
               (= "type" k)          [(keyword "user-config" (trim k)) (build-type-map    v)]
               :else                 [(keyword "user-config" (trim k)) v])) identity m))

(defn normalize
  "Given map *m* assumed to be a representation of the user configuration obtained
   by parsing a JSON file, returns a map that conforms to :user-config/config spec"
  [m]
  (let [user-config (w/walk  (fn [[k v]]
                               (cond
                                 (= "command" k) [(keyword k) (build-command-map v)]
                                 :else            [(keyword "user-config" k) v])) identity m)]

    (when-not (s/valid? :user-config/config user-config)
      (throw (ex-info "invalid user config" {:reason (s/explain-data :user-config/config user-config)})))

    user-config))

(comment
  (s/valid? :user-config/config {:user-config/server-port "a"}))


