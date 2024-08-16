(ns config
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as spec]
            [clojure.string :as str]))

;; config is a map built by merging default config settings and, when available, user defined settings
;; - User defined settings overload default settings
;; - User defined settings are provided as a YAML file

;; Config map must be validated by specs

(spec/def :config/server-port    number?)
(spec/def :config/root-dir-path  string?)
(spec/def :config/open-browser   boolean?)
(spec/def :config/browse-url     string?)


(spec/def :config.type.selector/name keyword?)
(spec/def :config.type.selector/arg  string?)
(spec/def :config.type/selectors  (spec/every-kv :config.type.selector/name :config.type.selector/arg))

(spec/def :config.type/definition (spec/keys :req [:config.type/selectors]))
(spec/def :config/types (spec/every-kv keyword? :config.type/definition))

(spec/def :config/map  (spec/keys :req [:config/server-port
                                        :config/root-dir-path
                                        :config/open-browser
                                        :config/browse-url]

                                  :opt [:config/types]))

(comment
  (def config-1 #:config{:server-port 12
                 :root-dir-path "/tmp"
                 :open-browser true
                 :browse-url "http://localhost"
                 :types  {:MY_FIRST_TYPE #:config.type{:selectors {:pred  "string"}}
                          :MY_SECOND_TYPE #:config.type{:selectors {:pred  "string"
                                                                    :other-pred "arg2"}}}})
  (get-in config-1 [:config/types :MY_FIRST_TYPE :config.type/selectors])
  
  (spec/valid? :config/map config-1)
  ;;
  )



(comment
  ;; loading and prasing a YAML file into a map

  (def conf (yaml/parse-stream (io/reader "./test/back/fixtures/file-1.yaml")))
  ;;
  )

