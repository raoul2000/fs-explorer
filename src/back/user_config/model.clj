(ns user-config.model
  (:require [clojure.spec.alpha :as s]))


;; spec ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; shape  example
  (def shape1 #:user-config{;; general settings
                            :server-port       8877
                            :open-browser      true
                            :browse-url       "http://localhost:8877"
                            :root-dir-path    "c:\\tmp\\my-root-fs"

                            ;; all commands

                            :command-index  {;; command is a string
                                             "notepad"     "notepad.exe"

                                             ;; command configured as a map
                                             "notepad++"   {:command "c:\\Program Files\\notepad++\\notepad++.exe"}

                                             ;; command configured as a map with a description
                                             "vscode"      {:command "c:\\Program Files\\vscode/code.exe"
                                                            :description  "start Visual Studio Code"}}

                            ;; item types
                            :type          {"text-file"   {:selector {:ends-with ".txt"}
                                                           :command  "notepad"}
                                            "readme"      {:selector "README.md"
                                                           :command  ["notepad" "notepad++" "vscode"]}
                                            "image"       {:selector {:ends-with [".jpg" ".jpeg"  ".png"]}}}})
  ;;
  )
;; general settings ----------------------------------------------------------------------------------------------------

;; server port number
(s/def :user-config/server-port    (s/and int? #(< 0 % 65353)))
;; open the browser on startup ?
(s/def :user-config/open-browser   boolean?)
;; what URL to open in the browser
(s/def :user-config/browse-url     string?)
;; path to the root folder for all relatives path 
(s/def :user-config/root-dir-path  string?)

;; command-index -------------------------------------------------------------------------------------------------------

(s/def :command/instruction string?)
(s/def :command/description string?)

(s/def :command-index/value (s/or :simple   :command/instruction
                                  :detailed (s/keys :req [:command/instruction]
                                                    :opt [:command/description])))

(s/def :user-config/command-index (s/map-of string? :command-index/value))

(comment

  (s/valid? :user-config/command-index {"notepad" "notepad.exe"})
  (s/valid? :user-config/command-index {"notepad" #:command{:instruction "notepad.exe"
                                                            :description "run notepas"}})

  ;;
  )

;; custom types 2 --------------

;; types are configured in a map where keys is the type name and value is the type definition
(s/def :user-config/type (s/every-kv string? 
                                     (partial s/valid? :type/definition)))

;; Type definition is a map with one required key : 'selector'
(s/def :type/definition (s/keys :req [:type/selector]))

(s/def :type/selector string?)

(comment
  (s/valid? :type/selector "file.txt")
  (s/valid? :type/definition {:type/selector "file.txt"})
  (s/valid? :type/definition {:type/selector true})
  (s/valid? :type/definition {:type/key "file.txt"})

  (s/valid? :user-config/type {"type 1" {:type/selector "file1.txt"}})

  ;;
  )

;; a type selector can also be a matcher
(s/def :type/selector (s/or :exact-match  string?
                            :filters      :selector/filters))

;; ... and a filters is a map, where keys are filter name and value are filter arg
(s/def :selector/filters (s/every-kv keyword?  :filter/arg))

;; filter arg is a string
(s/def :filter/arg string?)

(comment
  (s/valid? :selector/filters {:f1 "arg1"
                               :f2 "arg2"})

  (s/valid? :selector/filters {:f1 "arg1"
                               :f2 ["arg1" "arg2"]})

  (s/valid? :type/definition {:type/selector "file.txt"})
  (s/valid? :type/definition {:type/selector {:f1 "arg2"
                                              :f2 "arg2"}})
  ;;
  )

;; extend filter argument to a vector of strings
(s/def :filter/arg (s/or  :single-arg string?
                          :multi-arg (s/coll-of string?)))

(comment
  (s/valid?
   :type/definition {:type/selector {:f1 "arg1"
                                     :f2 ["ee"]}})

  (s/def ::my-map (s/keys :req [::key-string ::key-vector]))

  (s/def ::key-string (s/or :string string? :vector (s/coll-of string?)))
  (s/def ::key-vector (s/coll-of string?))

  (s/valid? ::my-map {"A" 1
                      ["1"] 1})
  (s/describe ::my-map)

  (s/def ::m2 (s/keys :req [::name]))
  (s/def ::name (s/or :string string?
                      :nulmber number?
                      :list-of-string (s/coll-of string?)))
  (s/valid? ::m2 {::name "bob"})
  (s/explain ::m2 {::name ["1"]})
  (s/explain ::m2 {::name true})

  (s/def ::m3 (s/every-kv string? (s/or :string string? :number number?)))
  (s/explain ::m3 {"e" 1
                   "r" "eer"
                   "t" :somek})

  ()

  ;;
  )


;; custom types --------------------------------------------------------------------------------------------------------

(s/def :string/single-or-list (s/or :single   string?
                                    :multiple (s/coll-of string?)))

(s/def :selector/starts-with   :string/single-or-list)
(s/def :selector/ends-with     :string/single-or-list)
(s/def :selector/equals        :string/single-or-list)
(s/def :selector/match         :string/single-or-list)

(s/def :type/selector (s/or :simple   string?
                            :detailed (s/keys :opt [:selector/starts-with
                                                    :selector/ends-with
                                                    :selector/equals
                                                    :selector/match])))
(s/def :type/command (s/or :single   string?
                           :multiple (s/coll-of string?)))

(s/def :type/value (s/keys :req [:type/selector]
                           :opt [:type/command]))

(s/def :user-config/type (s/map-of string? :type/value))

(comment
  (string? "ee")
  (s/valid? :user-config/type {:k #:type{:selector "readme.txt"}})
  (s/explain :user-config/type {"t" #:type{:selector "readme.txt"}})
  (s/valid? :user-config/type {"type1" #:type{:selector "ee"}})

  (s/explain :user-config/type {"type1" #:type{:selector #:selector{:starts-with 1}}})
  (s/valid? :user-config/type {"type1" #:type{:selector #:selector{:starts-with "XXX"
                                                                   :ends-with "txt"}}})

  ;;
  )


(s/def :action.selector/match string?)
(s/def :action.selector/equals string?)
(s/def :action/selector (s/or :equal string?
                              :map   (s/keys :opt [:action.selector/equals
                                                   :action.selector/match])))
(s/def :action/command      string?)
(s/def :action/def          (s/keys :req [:action/selector :action/command]))
(s/def :action/coll         (s/coll-of :action/def :kind vector?)) ;; could add :distinct true

(s/def :user-config/actions (s/coll-of :action/def :kind vector?))

(s/def :user-config/config         (s/keys :opt [:user-config/server-port
                                                 :user-config/open-browser
                                                 :user-config/browse-url
                                                 :user-config/root-dir-path
                                                 :user-config/actions]))