(ns actions
  (:require [clojure.data.json :as json]
            [clojure.spec.alpha :as s])
  (:import [java.util.regex PatternSyntaxException]))


(comment

  ;; TODO: find a good name !!
  ;;      - features, custom action, action ??....

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
  {::features [{:selector  "readme.md"
                :command   "open"}

               {:selector  "readme.txt"
                :command   "download"}]}

  ;; Meaning:
  ;; - when user clicks on file names "readme.md" , start the built-in command named "open"
  ;; - when user clicks on file names "readme.txt" , start the built-in command named "download"

  ;; let's spec this map

  (s/def :action/selector     string?)
  (s/def :action/command      string?)
  (s/def :action/def          (s/keys :req [:action/selector :action/command]))
  (s/def :action/coll         (s/coll-of :action/def :kind vector?)) ;; could add :distinct true


  (s/explain-str :action/def #:action{:selector  "readme.md"
                                      :command   "edit"})

  (s/explain-str :action/coll [#:action{:selector  "readme.md"
                                        :command   "edit"}
                               #:action{:selector  "readme.md"
                                        :command   "edit"}])

  ;; the selector's value could be a regular expression
  ;; - validation : it seems java and javascript regexp are quite similar based on https://en.wikipedia.org/wiki/Comparison_of_regular_expression_engines#Language_features
  ;;   so backend side could validate the regexp before sending it to the front for evaluation in the browser
  ;; - representation : it is entered as a string by the user and compiled into a regex by the back for validation and the front
  ;;   for evaluation.
  ;; 
  ;;

  ;; possible representation
  {:selector {:equals "readme.md"}}  ;; exact match. Same as {:selector "readme.md"}
  {:selector {:match ".*"}}          ;; regex match
  {:selector {:starts-with "read"}}  ;; partial match
  {:selector {:endss-with ".md"}}    ;; partial match
  ;; mix them all 
  {:selector {:match       "me"
              :starts-with "read"
              :ends-with   ".md"}}

  ;; let's try to spec the selector
  (s/def :action.selector/match string?)
  (s/def :action.selector/equals string?)
  (s/def :action/selector (s/or :equal string?
                                :map   (s/keys :opt [:action.selector/equals
                                                     :action.selector/match])))
  ;; following is same as above
  (s/def :action/command      string?)
  (s/def :action/def          (s/keys :req [:action/selector :action/command]))
  (s/def :action/coll         (s/coll-of :action/def :kind vector?))

  (s/valid? :action/coll [#:action{:command "open"
                                   :selector "readme.md"}])
  
  (s/valid? :action/coll [#:action{:command "open"
                                   :selector {:equals "readme.md"}}])
  
  (s/valid? :action/coll [#:action{:command "open"
                                   :selector {:equals "readme.md"
                                              :match  "someRegEx"}}])

  ;;
  )

(comment
  
  ;; BREAKING CHANGE !!

  ;; actions feature should be implemented on server side
  ;; - actual command line must not be exposed on client side
  ;; - more control on registered commands
  ;; - for regex selectors, validation and execution done on the same environment (Java) even
  ;;   if in theory there should be compatibility between Java and javascript
  ;; 
  ;; A command catalog should be configured where each command is identified by an id
  ;; Association between files and actions is done via this id
  ;;
  ;; for example:
  {:id "notepad"
   :command "notepad.exe"}
  
  ;; and thenfor the action :
  {:selector    "readme.txt"
   :command-id  "notepad"}
  
  
  ;;
  )

(comment

  ;; json / Clojure ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

  ;; working on user configured regexp ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; given this Re
  (def re1 ".*\\/A.*")

  ;; compile it into a regexp instance
  (type (re-pattern re1))
  ;; and use it to match

  (re-matches (re-pattern re1) "abc/Abd")
  (re-matches (re-pattern re1) "xxxx")

  (re-matches (re-pattern "txt") "readme.txt")
  (re-matches (re-pattern "abc") "readme.txt")
  (re-find  (re-pattern "txt") "readme.txt")
  (re-find  (re-pattern "abc") "readme.txt")

  (re-matches (re-pattern "e") "readme.txt")
  (re-matches (re-pattern "x") "readme.txt")
  (re-matches (re-pattern "x") "readme.txt")
  (re-matches (re-pattern "txt") "readme.md")

  ;; it can have capturing groups
  (re-find (re-pattern "(.*)\\/A(.*)")  "abcd/Abcd")

  ;; what happens if the regexp in syntaxically incorrect ?
  (try
    (re-pattern "*") ;; will throw
    (catch java.util.regex.PatternSyntaxException ex (.getMessage ex)))

  ;;
  )