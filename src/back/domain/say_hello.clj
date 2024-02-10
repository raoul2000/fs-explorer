(ns domain.say-hello
  (:require [clojure.string :as s]))


(defn say-hello [name polite?]
  (if (= "bob" name)
    ;; logic layer error must be thrown as ex-info
    (throw (ex-info "user not allowed" {:name name}))
    (format (if polite?
              "Good morning %s !"
              "Hi %s !") (if (s/blank? name) "stranger" name))))