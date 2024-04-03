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

                            :command-index  {"notepad"     "notepad.exe"
                                             "notepad++"   {:command "c:\\Program Files\\notepad++\\notepad++.exe"}
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