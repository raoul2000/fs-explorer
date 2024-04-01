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

  ;; and then for the action :
  {:selector    "readme.txt"
   :command-id  "notepad"}

  ;; Shape of the command catalog
  ;; Could we spe something like this ?
  {"my-command" {:command "notepad.exe"}}

  ;; yes, using s/map-of
  (s/def :command/instruction string?)
  (s/def :command/catalog (s/map-of string? :command/instruction))

  (s/valid? :command/catalog {"aa"  "notepad.exe"})

  ;; let's extend command definition over a simple string

  ;; simple case : provide command instruction
  (s/def :command/simple-definition string?)

  ;; more details : command defined as map
  (s/def :command/instruction string?)
  (s/def :command/description string?)
  (s/def :command/extended-definition (s/keys :req [:command/instruction]
                                              :opt [:command/description]))

  ;; a definition can be simple (string) or extended (map)
  (s/def :command/definition (s/or :simple :command/simple-definition
                                   :extended :command/extended-definition))

  (s/def :command/catalog (s/map-of string? :command/definition))

  ;; let's try
  (s/valid? :command/definition "notepad.exe")
  (s/valid? :command/definition #:command{:instruction "notepad.exe"
                                          :description "start edition wiht notepad.exe"})

  (s/valid? :command/catalog {"notepad"  "notepad.exe"
                              "notepad2" #:command{:instruction "notepad.exe"
                                                   :description "start edition wiht notepad.exe"}
                              "editor"  #:command{:instruction "notepad++.exe"}})

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

  ;; Problem : assuming we want to read a JSON file and parse it so to create a map where keys are string?
  ;; solution : do not provide a :key-fn arg as string is default type for keys
  (json/read-str "{\"colors\":[\"green\",\"red\",\"blue\"]}")


  ;; Now, as we saw above, command catalog is a map where keys are string. It is read from the user-config json file
  ;; where other keys are actual keys. We must find a way to parse the user-config JSON file and produce a map
  ;; with : 
  ;; - some keys as string (in command catalog)
  ;; - some keys as actual keys with distinct ns
  ;; for example :

  (def result #:user-config{:port 8080
                            :commands {"cmd1" #:command{:instruction "notepad.exe"}}})
  (:user-config/port result)
  (get-in result [:user-config/commands "cmd1" :command/instruction] result)

  ;; and this is the JSON read from user config file : 
  (def json "{\"port\": 8080, \"commands\" : {\"cmd1\" : {\"instruction\" : \"notepad.exe\"}}}")

  ;; in its simplest form, no namespace and no keywords keys
  (json/read-str json)

  ;; handle keyword conversion for the top level keys 
  (json/read-str json
                 :key-fn #(if (#{"port" "commands"} %)
                            (keyword %)
                            %))
  ;; problem is that the value of the :commands key needs specific transformation 

  (defn ->key [k]
    (cond
      (#{"port" "commands"} k)  (keyword "user-config" k)
      (#{"instruction"} k)      (keyword "command" k)
      :else k))

  (json/read-str json :key-fn ->key)

  ;; If it would get more complex, like for instance the same property name but with 2 different ns 
  ;; depending on its location in the trree, then it may be required to perform post process walk to
  ;; fix it.
  ;; see  https://github.com/clojure/data.json?tab=readme-ov-file#converting-keyvalue-types



  ;; working on user configured regexp ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; This is used for regexp based selector
  
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