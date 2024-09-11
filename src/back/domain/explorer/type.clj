(ns domain.explorer.type
  (:require [clojure.string :as s]))


(def file-selectors-catalog {:equals ;; ----------------------------------
                             (fn [val options file-m]
                               (= val (:name file-m)))

                             :starts-with ;; -----------------------------
                             (fn [val options file-m]
                               (s/starts-with? (:name file-m) val))

                             :ends-with ;; ------------------------------
                             (fn [val options file-m]
                               (s/ends-with? (:name file-m) val))})

(def file-selector-keys (keys file-selectors-catalog))

(defn create-selector-pred [m]
  (when-let [selector-key (some (set (keys m)) file-selector-keys)]
    (fn [file-m]
      (let [selector-val (get m selector-key)
            selector-fn  (get file-selectors-catalog selector-key)]
        (selector-fn selector-val m file-m)))))

(defn selector-match [file-m selector-m]
  (when-let [selected? (create-selector-pred selector-m)]
    (selected? file-m)))


(defn all-selectors-match [selectors-xs file-m]
  (let [selector-match? (partial selector-match file-m)]
    (every? selector-match? selectors-xs)))

(defn type-match [type-def-m file-m]
  (or
   ;; type with no selector always match
   (not (:selectors type-def-m))
   ;; ... or all selectors must match 
   (all-selectors-match (:selectors type-def-m) file-m)))
(def type-no-match (complement type-match))

(defn select-type [file-m type-def-xs]
  (first (drop-while #(type-no-match % file-m) type-def-xs)))

(defn infer-type
  "Given a map *file-m* and a seq of type definitions, add key `:type` to *file-m* if
     it matches a type definition or returns it unchanged."
  [type-def-xs file-m]

  (tap> {:type-def-xs type-def-xs})
  (if-let [infered-type (select-type file-m type-def-xs)]
    (assoc file-m :type (:config.type/name infered-type))
    file-m))

