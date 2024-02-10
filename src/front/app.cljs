(ns app
  (:require [timer :as timer]))



(defn run []
  (js/console.log "boo !")
  (timer/render "root"))

;;  Lifecycle Hooks =================================
;; see https://shadow-cljs.github.io/docs/UsersGuide.html#_lifecycle_hooks

(defn ^:dev/before-load stop []
  (js/console.log "/before-load"))


(defn ^:dev/after-load start []
  (js/console.log "after-load")
  (run))

(defn ^:dev/before-load-async async-stop [done]
  (js/console.log "(async) stop ")
  (js/setTimeout
   (fn []
     (js/console.log "(async) stop complete")
     (done))))

(defn ^:dev/after-load-async async-start [done]
  (js/console.log "(async) start")
  (js/setTimeout
   (fn []
     (js/console.log "(async) start complete")
     (done))))