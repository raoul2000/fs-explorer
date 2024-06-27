(ns spec-config
  (:require [clojure.spec.alpha :as s]))

;; the user config map should look like this

(comment
  
  (def shape1 #:user-config{;; general settings
                            :server-port       8877
                            :open-browser      true
                            :browse-url       "http://localhost:8877"
                            :root-dir-path    "c:\\tmp\\my-root-fs"
  
                              ;; all commands
  
                            :command-index  {;; command is a string (short form)
                                             "notepad"     "notepad.exe"
  
                                               ;; command configured as a map (long form)
                                             "notepad++"   {:command      "c:\\Program Files\\notepad++\\notepad++.exe"}
  
                                               ;; command configured as a map with a description
                                             "vscode"      {:command      "c:\\Program Files\\vscode/code.exe"
                                                            :description  "start Visual Studio Code"}}
  
                              ;; item types
                            :type          {"text-file"   {:selector {:ends-with ".txt"}
                                                           :command  "notepad"}
                                            "readme"      {:selector {:equals "README.md"}
                                                           :command  ["notepad" "notepad++" "vscode"]}
                                            "image"       {:selector {:ends-with [".jpg" ".jpeg"  ".png"]}}}})
  
  ;; Let's try to spec this shape

  ;; starting by the :type
  (s/def :user-config/type (s/every-kv string? :type/definition))
  (s/def :type/definition (s/keys :req [:type/selector]))
  (s/def :type/selector   (s/every-kv keyword? :selector/filter))
  (s/def :selector/filter (s/every-kv keyword? (s/or :simple    string?
                                                     :composite (s/coll-of string?))))

  (s/valid? (s/coll-of string?) ["1" "2"])
  (s/valid? :selector/filter {:k1 "ee" :k2 "bb"})
  (s/valid? :selector/filter {:k1 ["1" "2"] :k2 ["a" "b"]})
  (s/valid? :selector/filter {:k1 "a" :k2 ["a" "b"]})

  (s/valid? :type/selector {:type/selector {:k1 ["ee"]
                                            :k2 "rr"
                                            :k3 true}})
  
  (s/explain :type/definition {:type/selector {:k1 ["ee"]
                                            :k2 "rr"
                                            :k3 true}})


  ;; let's test
  (s/valid? :user-config/type {"type1" {:type/selector "file.txt"}})
  (s/explain :user-config/type {"type1" {:type/selector {:filter "arr"}}})
  (s/explain-data :user-config/type {"type1" {:type/selector {:filter true}}})


  ;;
  )