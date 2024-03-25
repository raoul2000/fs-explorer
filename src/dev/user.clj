(ns user
  (:require [portal.api :as p]
            [clojure.tools.namespace.repl :refer (refresh refresh-all set-refresh-dirs)]
            [integrant.core :as ig]
            [system :as sys]
            [clojure.test :as test]
            [io.pedestal.http :as http]
            [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [user-config :as user-conf]))

  ;; avoid reloading of dev ns
(set-refresh-dirs (str (fs/path (fs/cwd) "src" "back"))
                  (str (fs/path (fs/cwd) "src" "shared")))

(comment

  ;; portal ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; see https://github.com/djblue/portal#api for setup

  (def p (p/open {:launcher :vs-code}))
  (add-tap #'p/submit)

  (tap> "hello")
  (p/clear)


  
  ;; system ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; the system configuration
  (def system-config (-> sys/config
                         (assoc :app/user-config {:user-config/root-dir-path "c:\\tmp\\test_1"
                                                  :user-config/open-browser  false
                                                  :user-config/actions [{:selector  {:equal "package.json"}
                                                                         :command  "notepad.exe"}

                                                                        {:selector  "readme.md"
                                                                         :command   "dummy"}

                                                                        {:selector  "readme.txt"
                                                                         :command   "open"}

                                                                        {:selector "README"
                                                                         :command "c:\\program files\\notepad++\\notepad++.exe"}]})
                         #_(assoc-in [:app/config    :open-browser?]    false)
                         #_(assoc-in [:app/config    :polite?]          true)
                         (assoc-in [:server/server ::http/join?]   false)))

  (s/valid? :user-config/config {:user-config/root-dir-path "tmp"
                                 :user-config/open-browser  false})
  (s/explain :user-config/config {:user-config/root-dir-path "./test"
                                  :user-config/open-browser  false})

  ;; start the system
  (def system (ig/init system-config))

  ;; stop the system
  (ig/halt! system)

;; tools.namespace ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; see https://github.com/clojure/tools.namespace/
  ;; see https://cognitect.com/blog/2013/06/04/clojure-workflow-reloaded

  ;; NOTE : set-refresh-dirs above
  (refresh)
  (refresh-all)

  
  ;; tests ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  (require 'server.routes-test
           'server.handler.greet-test :reload-all)

  (test/run-tests 'server.routes-test
                  'server.handler.greet-test)

  (test/run-all-tests)


  ;;
  )


