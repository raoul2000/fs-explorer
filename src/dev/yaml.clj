(ns yaml
  (:require [clj-yaml.core :as yaml]
            [babashka.fs :as fs]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.walk :as w]
            [clojure.data.json :as json]))


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

(comment
  ;; reading and parsing yaml file into clojure map and then serializing it into EDN
  ;; adds reader tags

  (def yaml-input  [""
                    "root-dir-path: c:\\dev\\ws\\lab\\fs-explorer\\test\\back\\fixtures"
                    "server-port: 8882"
                    "open-browser : true"
                    "browse-url: http://localhost:8882"
                    "commands :"
                    "  MY_FIRST_COMMAND:"
                    "    program: c:\\program file\\notepad.exe   # this is an inline comment"
                    "    label: run first command"
                    "    args : "
                    "      - arg 1"
                    "      - 12"
                    "      - arg 3"
                    "  # this is another comment"
                    "  MY_SECOND_COMMAND:"
                    "    program: notepad.exe"
                    "    label: run second command"
                    "    args: "
                    "      - arg 1"
                    "      - arg 2"
                    "      - {INPUT_FILE}"
                    "types:"
                    "  MY_FIRST_TYPE:"
                    "    selector:"
                    "      match-regexp: .*/README.md$"
                    "      ends-with: md"
                    "      starts-with: /R"
                    "      equals: /README.md"
                    "      equals-ignore-case: /ReadMe.MD"
                    "    actions:"
                    "      - MY_FIRST_COMMAND : "
                    "        trigger:"
                    "          on-click: true"
                    "          on-double-click: false "
                    "      - MY_SECOND_COMMAND"
                    "  MY_SECOND_TYPE:"
                    "    selector:"
                    "      ends-with-ignore-case: txt"])

  (def parsed-data (yaml/parse-string (s/join "\n" yaml-input)))
  (print (json/write-str parsed-data :escape-slash false))
  ;; json is ok ..

  (print (pr-str parsed-data))
  ;; bnut EDN includes reader tag that can't be evaluated by clojurescript
  ;; For example ':#ordered/map' below
  ;;
  ;; =>  #ordered/map ([:root-dir-path "c:\\dev\\ws\\lab\\fs-explorer\\test\\back\\fixtures"] 
  ;;     [:server-port 8882] [:open-browser true] [:browse-url "http://localhost:8882"]  
  ;;     [:commands #ordered/map ([:MY_FIRST_COMMAND #ordered/map ([:program "c:\\program file\\notepad.exe"] 
  ;;     etc ...

  ;; To preserve order, we must use an array

  ;; Order preservation is important for types, because type inference algo is going to
  ;; process each type in the order they are configured, and will stop after first selector match
  ;; Selector order is also important for the same reason.

  (def yaml-input-2  ["types:"
                      "  - name: MY_FIRST_TYPE"
                      "    selector:"
                      "      - match-regexp: .*/README.md$"
                      "      - ends-with: md"
                      "      - starts-with: /R"
                      "      - equals: /README.md"
                      "      - equals-ignore-case: /ReadMe.MD"
                      "    actions:"
                      "      - MY_FIRST_COMMAND : "
                      "        trigger:"
                      "          on-click: true"
                      "          on-double-click: false "
                      "      - MY_SECOND_COMMAND"
                      "  - name: MY_SECOND_TYPE"
                      "    selector:"
                      "      - ends-with-ignore-case: txt"])

  (yaml/parse-string (s/join "\n" yaml-input-2))

  (def result {:types
               ({:name "MY_FIRST_TYPE",
                 :selector
                 '({:match-regexp ".*/README.md$"}
                   {:ends-with "md"}
                   {:starts-with "/R"}
                   {:equals "/README.md"}
                   {:equals-ignore-case "/ReadMe.MD"}),
                 :actions '({:MY_FIRST_COMMAND nil, :trigger {:on-click true, :on-double-click false}} "MY_SECOND_COMMAND")}
                {:name "MY_SECOND_TYPE", :selector '({:ends-with-ignore-case "txt"})})})
  ;;
  )

