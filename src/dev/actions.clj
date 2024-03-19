(ns actions
  (:require  [clojure.data.json :as json]))


(comment

  ;; TODO: find a good name !!
  ;;      - features, custom action, ....

  ;; describing an action attached to a file or a folder.
  ;; - need to select the target (file or folder)
  ;;   - by name ? (complete match)
  ;;   - by partial match (startsWith, endsWith)
  ;;   - RegExp match
  ;;      - profit capturing groups ?

  {:triggers [{:selector      {:type  [:file :dir]
                               :match "txt"}
               :action        {:name  "os.open"}}]}

  ;; a simple version to start : 
  {::features [{:selector {:name "readme.md"}
                :command   "edit"}

               {:selector {:name "readme.txt"}
                :command   "edit"}]}

  ;; what happens when the map contains an array of keys
  (json/write-str {:colors [:green :red :blue]}  :escape-slash false)
  ;; ... they are turned into string

  ;; and the other way around ?
  (json/read-str "{\"colors\":[\"green\",\"red\",\"blue\"]}"
                 :key-fn   #(keyword %)
                 ;; must process array for key :colors in order to not get string
                 ;; but keywords
                 :value-fn (fn [k v]
                             (if (and (seq v)
                                      (= k :colors))
                               (mapv keyword v)
                               v)))

  ;; working on user configured regexp

  ;; given this Re
  (def re1 ".*\\/A.*")

  ;; compile it into a regexp instance
  (type (re-pattern re1))
  ;; and use it to match

  (re-find (re-pattern re1) "abc/Abd")
  (re-find (re-pattern "txt") "readme.txt")
  (re-find (re-pattern "txt") "readme.md")

  ;; it can have capturing groups
  (re-find (re-pattern "(.*)\\/A(.*)")  "abcd/Abcd")

  ;; what happens if the regexp in syntaxically incorrect ?
  (try
    (re-pattern "*") ;; will throw
    (catch java.util.regex.PatternSyntaxException ex (.getMessage ex)))

  ;;
  )