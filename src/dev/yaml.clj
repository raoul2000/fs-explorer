(ns yaml
  (:require [clj-yaml.core :as yaml]
            [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.walk :as w]))


(comment

  (slurp "./test/back/fixtures/file-1.yaml")

  (def conf (yaml/parse-stream (io/reader "./test/back/fixtures/file-1.yaml")))

  (:root-dir-path conf)

  ;; list all declared command Ids as string
  (map name (keys (:commands conf)))

  ;; list all declared types as string
  (map name (keys (:types conf)))

  ;; list type selectors for type MY_FIRST_TYPE
  (get-in conf [:types :MY_FIRST_TYPE :selector])

  (def selector-fns {:match-regexp            (fn [arg s]
                                                (re-matches (re-pattern arg) s))
                     :equals                  =
                     :equals-ignore-case      (fn [arg s]
                                                (= (s/lower-case arg) (s/lower-case s)))
                     :ends-with               (fn [arg s]
                                                (s/ends-with? s arg))
                     :ends-with-ignore-case   (fn [arg s]
                                                (s/ends-with? (s/lower-case s) (s/lower-case arg)))
                     :starts-with             (fn [arg s]
                                                (s/starts-with? s arg))
                     :starts-with-ignore-case (fn [arg s]
                                                (s/starts-with? (s/lower-case s) (s/lower-case arg)))})


  ;; apply  all selectors for a given type (here MY_FIRST_TYPE)
  (map (fn [[pred arg]]
         (when-let [selector-fn (get selector-fns pred)]
           (selector-fn arg  "/README.md")))
       (get-in conf [:types :MY_FIRST_TYPE :selector]))

  ;; selector type matches if all selectors are truthy
  (every? identity '("/README.md" true true true true))


  (defn type-does-not-match [selector-map s]
    (fn [[_type-name type-def]]
      (let [selector-results (map (fn [[pred arg]]
                                    (when-let [selector-fn (get selector-map pred)]
                                      (selector-fn arg  s)))
                                  (:selector type-def))]
        (not (every? identity selector-results)))))

  (defn find-type [conf selector-map s]
    (drop-while (type-does-not-match selector-map s) (:types conf)))

  (find-type conf selector-fns "/README.md")
  (find-type conf selector-fns "/README.txt")
  (find-type conf selector-fns "/README.xml")

  ;;
  )

(comment

  ;; the map returned by yaml/parse-stream turn yaml keys into Clojure keywords by default but this is
  ;; not what we need. 
  ;; - keywords must be namespaces
  ;; - some keys should be turned into string (type ID)

  (def conf (yaml/parse-stream (io/reader "./test/back/fixtures/file-1.yaml")))



  (defn build-commands [m]
    (w/walk (fn [[k v]]
              [k (into {} (map (fn [[k2 v2]]
                                 [(add-ns-to-key "command" k2) v2]) v))]) identity m))

  (defn build-types [m]
    (w/walk (fn [[k v]]
              [k (into {} (map (fn [[k2 v2]]
                                 [(add-ns-to-key "type" k2) v2]) v))]) identity m))


  (defn add-ns-to-map 
    "Add *ns-name* namespace to all keys of map *m*.
     When a key is a string it is converted to a keyword.
     When a key is a keyword already with a namespace, it is replaced by the given ns"
    [ns-name m]
    (into {} (map (fn [[k2 v2]]
                    [(add-ns-to-key ns-name k2) v2]) m)))
  
  (add-ns-to-map "bob" {:a 1})
  (add-ns-to-map "bob" {:my-ns/a 1})
  (add-ns-to-map "bob" {"a" 1})

  (defn add-ns-to-key [ns-name k]
    (keyword ns-name (name k)))


  (w/walk (fn [[k v]]
            (let [ns-key (add-ns-to-key "user-config" k)]
              [ns-key (case ns-key
                        :user-config/commands  (build-commands v)
                        :user-config/types     (build-types   v)
                        v)])) identity conf)


  ;;
  )

