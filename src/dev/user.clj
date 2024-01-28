(ns user
  (:require [portal.api :as p]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [integrant.core :as ig]
            [system :refer (config)]))

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

  (refresh)
  (refresh-all)


  ;; system ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

  ;; start the system
  (def system (ig/init config))

  ;; stop the system
  (ig/halt! system)


  ;;
  )

