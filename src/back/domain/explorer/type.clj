(ns domain.explorer.type
  (:require [babashka.fs :as fs]
            [clojure.string :as s])
  (:import [java.util.regex PatternSyntaxException]))

(defn compile-regexp [s]
  (try
    (re-pattern s)
    (catch PatternSyntaxException e
      (throw (ex-info (format "invalid Regular Exception syntax") {:regexp s
                                                                   :cause   (.getMessage e)})))))

(def compile-regexp-memo (memoize compile-regexp))

(defn property-val [file-m {property :selector/property} default-property]
  (get file-m (keyword "file" (or property default-property))))

(def file-selectors-catalog #:selector{:equals ;; ----------------------------------
                                       (fn [val options file-m]
                                         (when-let [item-property-value (property-val file-m options "name")]
                                           (some #(= item-property-value %) (if (coll? val) val [val]))))

                                       :starts-with ;; -----------------------------
                                       (fn [val options file-m]
                                         (when-let [item-property-value (property-val file-m options "name")]
                                           (some #(s/starts-with? item-property-value %) (if (coll? val) val [val]))))

                                       :ends-with ;; ------------------------------
                                       (fn [val options file-m]
                                         (when-let [item-property-value (property-val file-m options "name")]
                                           (some #(s/ends-with? item-property-value %) (if (coll? val) val [val]))))

                                       :is-directory ;; ----------------------------------
                                       (fn [val options file-m]
                                         (= val (fs/directory? (:file/path file-m))))

                                       :matches-regexp ;; --------------------------------------
                                       (fn [val options file-m]
                                         (when-let [item-property-value (property-val file-m options "name")]
                                           (boolean (some #(re-matches (compile-regexp-memo %) item-property-value)
                                                          (if (coll? val) val [val])))))})

(def file-selector-keys (keys file-selectors-catalog))

(defn create-selector-pred
  "When *m* contains a key in the file-selectors-catalog, returns a predicate function
   to evaluate if a *file-m* matches this selector. Otherwise returns *nil*."
  [m]
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

(defn type-match
  "Returns True if type definition *type-def-map* matches *file-m*."
  [type-def-m file-m]
  (or
   ;; type with no selector always match
   (not (:type/selectors type-def-m))
   ;; ... or all selectors must match 
   (all-selectors-match (:type/selectors type-def-m) file-m)))
(def type-no-match (complement type-match))

(defn select-type
  "Returns the type definition map that matches *file-m* or *nil* if no type match is found."
  [file-m type-def-xs]
  (first (drop-while #(type-no-match % file-m) type-def-xs)))

(defn infer-type
  "Given a map *file-m* and a seq of type definitions, add key `:file/type` to *file-m* if
   it matches a type definition or returns it unchanged."
  [type-def-xs file-m]
  (if-let [infered-type (select-type file-m type-def-xs)]
    (assoc file-m :file/type (:type/name infered-type))
    file-m))


(defn ignored-type [type-def-xs file-m]
  (when-let [file-type (get file-m :file/type)]
    (some #(and (= file-type (:type/name %))
                (get % :type/ignore)) type-def-xs)))

