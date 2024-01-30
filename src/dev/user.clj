(ns user
  (:require [portal.api :as p]
            [clojure.tools.namespace.repl :refer (refresh refresh-all set-refresh-dirs)]
            [integrant.core :as ig]
            [system :refer (config)]
            [clojure.test :as test]
            [io.pedestal.http :as http]
            [babashka.fs :as fs]))

(comment

  ;; portal ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; see https://github.com/djblue/portal#api for setup

  (def p (p/open {:launcher :vs-code}))
  (add-tap #'p/submit)

  (tap> "hello")
  (p/clear)


  ;; tools.namespace ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; see https://github.com/clojure/tools.namespace/
  ;; see https://cognitect.com/blog/2013/06/04/clojure-workflow-reloaded
  
  ;; avoid reloading of dev ns
  (set-refresh-dirs (str (fs/path (fs/cwd) "src" "back")))
  (refresh)
  (refresh-all)

  ;; system ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; start the system
  (def system (ig/init (-> config
                           (assoc-in [:app/config :polite?]          true)
                           (assoc-in [:server/server ::http/join?]   false))))

  ;; stop the system
  (ig/halt! system)

  ;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (require 'server.routes-test
           'server.handler.greet-test :reload-all)

  (test/run-tests 'server.routes-test
                  'server.handler.greet-test)

  (test/run-all-tests)

  ;;
  )


