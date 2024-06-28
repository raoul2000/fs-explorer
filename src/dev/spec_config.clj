(ns spec-config
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

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
                                            ;;
  )

(s/def :string/not-blank (s/and string? (complement str/blank?)))
(s/def :coll/non-empty-string-list (s/coll-of :string/not-blank :min-count 1))

(s/def :filter/name                keyword?)
(s/def :filter/arg                 (s/or  :string      :string/not-blank
                                          :string-list :coll/non-empty-string-list))
(s/def :selector/filters           (s/every-kv :filter/name :filter/arg))
(s/def :type/selector              (s/or  :strict-equals :string/not-blank
                                          :filter        :selector/filters))
(s/def :type/definition            (s/keys :req [:type/selector]))
(s/def :type/name                  :string/not-blank)
(s/def :user-config/type           (s/every-kv :type/name :type/definition))



(s/def :user-config/definition (s/keys :opt [:user-config/type]))

(comment

  ;; test _____________________
  (s/explain :selector/filters {:f1 "arg"
                                :f2 ["1"]})
  ;; __________________________



  ;; test _____________________
  (s/valid? :type/definition {:type/selector "ee"})
  (s/explain :type/definition {:type/selector ["ee"]})

  (s/valid? :type/definition {:type/selector {:k1 "s" :k2 ["s" "s"]}})
  (s/explain :type/definition {:type/selector {:k1 "s" :k2 ["s" "s" 1]}})
  (s/explain :type/definition {:type/selector {"stringkey" "s" :k2 ["s" "s"]}})
  ;; _____________________



  (s/valid? :user-config/type {"type1" {:type/selector "ee"}
                               "type2" {:type/selector {:start-with ["1" "2"]}}})






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

(comment
  ;; we want to write a spec for this shape

  (def shape-1 {:k1 "string"})
  (def shape-2 {:k1 ["s1" "s2"]})

  ;; The key :k1 can be a string or a vector of strings

  (s/def ::k1 (s/or :string string? :vector vector?))
  (s/def ::mymap (s/every-kv keyword? ::k1))

  (s/valid? ::mymap {:k1 "s"})
  (s/valid? ::mymap {:k1 ["s"]})

  (s/valid? ::mymap {:k1 "s"
                     :k2 "s"})

  (s/valid? ::mymap {:k1 ["s"]
                     :k2 ["s"]})

  (s/valid? ::mymap {:k1 "s"
                     :k2 ["s"]})

  (s/valid? ::mymap {:k1 ["s"]
                     :k2 "s"})

  (s/valid? ::mymap {:k1 ["s"]
                     :k2 true})
                     ;; false

  ;;

  ;; let's use the ::mymap spec in another spec
  (def shape-3 {"str" {:k1  "s"
                       :k2  [1 2 3]}})

  (s/def ::shape-3 (s/every-kv string? ::mymap))

  (s/valid? ::shape-3 {"str" {:k1  "s" :k2  [1 2 3]}})
  (s/valid? ::shape-3 {"str" {:k1  true}})
  (s/explain ::shape-3 {"str" {:k1  true}})

  (s/valid? ::shape-3 {:k0 {:k1  "s" :k2  [1 2 3]}})
  (s/explain ::shape-3 {:k0 {:k1  "s" :k2  [1 2 3]}})

  (s/valid? ::shape-3 {"str" {:k1  "s" :k2  [1 2 3]}
                       "s2"  {:k1  "s" :k2  "s2"}
                       "s3"  {:k1  ["s"] :k2  1}})


  ;;
  )